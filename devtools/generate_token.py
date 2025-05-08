import os
import subprocess
import sys
import time
from datetime import datetime, timedelta

# === Auto-install required libraries ===
try:
    import jwt
    import pyperclip
    from rich.console import Console
    from rich.prompt import Prompt
    from rich.panel import Panel
    from rich.table import Table
    from rich.text import Text
    from rich.progress import Progress, SpinnerColumn, TextColumn
except ModuleNotFoundError:
    subprocess.check_call([
        sys.executable, "-m", "pip", "install",
        "click", "pyjwt", "cryptography", "rich", "pyperclip"
    ])
    import jwt
    import pyperclip
    from rich.console import Console
    from rich.prompt import Prompt
    from rich.panel import Panel
    from rich.table import Table
    from rich.text import Text
    from rich.progress import Progress, SpinnerColumn, TextColumn

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec

# === Paths ===
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
KEY_DIR = os.path.join(BASE_DIR, "devtools", "jwt")
DOCKER_DIR = os.path.join(BASE_DIR, "devtools", "docker")
COMPOSE_DEV = os.path.join(DOCKER_DIR, "docker-compose.yaml")
COMPOSE_IMG = os.path.join(DOCKER_DIR, "docker-compose.image.yaml")
PRIVATE_KEY_PATH = os.path.join(KEY_DIR, "ec256-private.pem")
PUBLIC_KEY_DER_PATH = os.path.join(KEY_DIR, "ec256-public.der")
PUBLIC_KEY_PEM_PATH = os.path.join(KEY_DIR, "ec256-public.pem")

ISSUER = "dbaccess-api"
AUDIENCE = "dbaccess-client"
SUBJECTS = ["alice", "bob", "charlie"]
console = Console()

LOGO = r"""
  ________              __                         __                          ____  ____.________
 /  _____/  ____  _____/  |_  ___________    ____ |  | __ ___________  ____   |    |/ _/_   \   _  \ 
/   \  ____/ __ \ \__  \   __\/ __ \_  __ \  / ___\|  |/ // __ \_  __ \/ __ \  |      <  |   ||  | \/ 
\    \_\  \  ___/  / __ \|  | \  ___/|  | \/ / /_/  >    <\  ___/|  | \/ /_/ /  |    |  \ |   ||  |__  
 \______  /\___  >(____  /__|  \___  >__|    \___  /__|_ \\___  >__|  \____/   |____|__ \|___| \____/  
        \/     \/      \/          \/        /_____/     \/    \/                   \/               
"""

def keys_exist():
    return os.path.exists(PRIVATE_KEY_PATH) and os.path.exists(PUBLIC_KEY_DER_PATH)

def generate_keys():
    os.makedirs(KEY_DIR, exist_ok=True)
    console.print("[bold cyan]\U0001F510 Generating EC256 key pair...[/bold cyan]")
    private_key = ec.generate_private_key(ec.SECP256R1(), default_backend())
    pem = private_key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.TraditionalOpenSSL, serialization.NoEncryption())

    with open(PRIVATE_KEY_PATH, "wb") as f:
        f.write(pem)

    pub_key = private_key.public_key()
    pub_pem = pub_key.public_bytes(serialization.Encoding.PEM, serialization.PublicFormat.SubjectPublicKeyInfo)
    pub_der = pub_key.public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)

    with open(PUBLIC_KEY_PEM_PATH, "wb") as f:
        f.write(pub_pem)
    with open(PUBLIC_KEY_DER_PATH, "wb") as f:
        f.write(pub_der)

    console.print("[green]\u2705 Keys generated successfully![/green]")
    input("Press [Enter] to return to menu...")

def load_private_key():
    with open(PRIVATE_KEY_PATH, "rb") as key_file:
        return serialization.load_pem_private_key(key_file.read(), password=None, backend=default_backend())

def show_keys():
    if not keys_exist():
        console.print("[red]\u274C No keys found![/red]")
    else:
        table = Table(title="\U0001F511 Key Info", box=None)
        table.add_column("Type", style="cyan")
        table.add_column("Path", style="green")
        table.add_row("Private Key", PRIVATE_KEY_PATH)
        table.add_row("Public PEM", PUBLIC_KEY_PEM_PATH)
        table.add_row("Public DER", PUBLIC_KEY_DER_PATH)
        console.print(table)
    input("Press [Enter] to return to menu...")

def generate_token(subject):
    if not keys_exist():
        console.print("[red]\u274C Keys missing. Generate them first![/red]")
        return

    now = datetime.utcnow()
    exp = now + timedelta(minutes=5)
    claims = {
        "sub": subject,
        "iat": now,
        "exp": exp,
        "iss": ISSUER,
        "aud": [AUDIENCE]
    }

    token = jwt.encode(claims, load_private_key(), algorithm="ES256")
    panel = Panel.fit(token, title="JWT Token", subtitle="Paste in Swagger > Authorize", style="cyan")
    console.print(panel)
    try:
        pyperclip.copy(token)
        console.print("[green]\U0001F4CB Token copied to clipboard![/green]")
    except Exception:
        console.print("[yellow]\u26A0\uFE0F Could not copy to clipboard[/yellow]")

    table = Table(title="\U0001F4DC Token Claims", box=None)
    table.add_column("Claim", style="magenta")
    table.add_column("Value", style="white")
    for k in claims:
        table.add_row(k, str(claims[k]))
    console.print(table)
    input("Press [Enter] to return to menu...")

def clean_existing_container(name):
    containers = subprocess.run(["docker", "ps", "-a", "--format", "{{.Names}}"], capture_output=True, text=True).stdout.splitlines()
    if name in containers:
        console.print(f"[yellow]⚠️ Removing existing container '{name}'[/yellow]")
        subprocess.run(["docker", "rm", "-f", name])

def run_compose(file_path, project_name, message):
    if not os.path.exists(file_path):
        console.print(f"[red]\u274C Missing Docker Compose file: {file_path}[/red]")
        input("Press [Enter] to return to menu...")
        return
    if not keys_exist():
        console.print("[red]\u274C Keys are required before starting the project.[/red]")
        input("Press [Enter] to return to menu...")
        return

    clean_existing_container("safe-access-mongo")
    clean_existing_container("safe-access-postgres")

    with Progress(SpinnerColumn(), TextColumn("[progress.description]{task.description}"), transient=True) as progress:
        progress.add_task(description="Launching Docker Compose...", total=None)
        subprocess.Popen(["docker-compose", "-f", file_path, "-p", project_name, "up", "--build"])
        time.sleep(3)

    console.print(Panel.fit(f"[green]\u2705 {project_name} started![/green]\n\n{message}", title="\U0001F7E2 Running", style="bold green"))
    input("Press [Enter] to return to menu...")

def stop_compose(compose_file, project_name):
    full_path = os.path.abspath(os.path.join(DOCKER_DIR, compose_file))
    if not os.path.exists(full_path):
        console.print(f"[yellow]\u26A0\uFE0F Compose file not found: {full_path}[/yellow]")
    else:
        subprocess.run(["docker-compose", "-f", full_path, "-p", project_name, "down"], check=False)
        console.print(f"[red]\U0001F534 '{project_name}' stopped successfully.[/red]")
    input("\nPress [Enter] to return to menu...")

def menu():
    while True:
        console.clear()
        console.print(Panel(LOGO, title="safe-temporary-db-access", style="bold magenta"))
        console.print("GitHub: [link=https://github.com/CamilYed/safe-temporary-db-access]https://github.com/CamilYed/safe-temporary-db-access[/link]\n")

        console.print("[bold green]1.[/bold green] Install Python Dependencies")
        console.print("[bold green]2.[/bold green] Generate EC256 Keys (if missing)")
        console.print("[bold green]3.[/bold green] Show Key Paths")
        console.print("[bold green]4.[/bold green] Generate JWT Token")
        console.print("[bold cyan]5.[/bold cyan] Run Docker Compose (Dev Build)")
        console.print("[bold cyan]6.[/bold cyan] Run Docker Compose (Prebuilt Image)")
        console.print("[bold red]7.[/bold red] Stop Docker Compose (Dev)")
        console.print("[bold red]8.[/bold red] Stop Docker Compose (Prebuilt)")
        console.print("[bold magenta]9.[/bold magenta] Exit")

        choice = Prompt.ask("\n[?] Choose an option", choices=[str(i) for i in range(1, 10)], default="9")

        if choice == "1":
            subprocess.check_call([sys.executable, "-m", "pip", "install", "-r", os.path.join(BASE_DIR, "devtools", "requirements.txt")])
        elif choice == "2":
            generate_keys()
        elif choice == "3":
            show_keys()
        elif choice == "4":
            subject = Prompt.ask("Enter subject", choices=SUBJECTS)
            generate_token(subject)
        elif choice == "5":
            run_compose(COMPOSE_DEV, "safe-access-dev", "Run IntelliJ IDEA with profile: [bold cyan]dev[/bold cyan]")
        elif choice == "6":
            run_compose(COMPOSE_IMG, "safe-access-img", "Open in browser: [bold]http://127.0.0.1:8080/swagger-ui/index.html[/bold]")
        elif choice == "7":
            stop_compose("docker-compose.yaml", "safe-access-dev")
        elif choice == "8":
            stop_compose("docker-compose.image.yaml", "safe-access-img")
        elif choice == "9":
            console.print("\n\U0001F44B [bold green]Goodbye and good hacking![/bold green]")
            break

if __name__ == "__main__":
    menu()
