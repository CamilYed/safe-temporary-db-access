# generate_token.py
import os
import subprocess
import sys
import time
from datetime import datetime, timedelta, timezone
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
from cryptography.hazmat.primitives.asymmetric import ec, rsa

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
   _____          __         _______                                                         _____   ____                                       
  / ____|        / _|       |__   __|                                                       |  __ \ |  _ \       /\                             
 | (___    __ _ | |_  ___      | |  ___  _ __ ___   _ __    ___   _ __  __ _  _ __  _   _   | |  | || |_) |     /  \    ___  ___  ___  ___  ___ 
  \___ \  / _` ||  _|/ _ \     | | / _ \| '_ ` _ \ | '_ \  / _ \ | '__|/ _` || '__|| | | |  | |  | ||  _ <     / /\ \  / __|/ __|/ _ \/ __|/ __|
  ____) || (_| || | |  __/     | ||  __/| | | | | || |_) || (_) || |  | (_| || |   | |_| |  | |__| || |_) |   / ____ \| (__| (__|  __/\__ \\__ \
 |_____/  \__,_||_|  \___|     |_| \___||_| |_| |_|| .__/  \___/ |_|   \__,_||_|    \__, |  |_____/ |____/   /_/    \_\\___|\___|\___||___/|___/
                                                   | |                               __/ |                                                      
                                                   |_|                              |___/                                                                                                    
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

    now = datetime.now(timezone.utc)
    exp = now + timedelta(minutes=5)
    claims = {
        "sub": subject,
        "iat": now,
        "exp": exp,
        "iss": ISSUER,
        "aud": [AUDIENCE]
    }

    token = jwt.encode(claims, load_private_key(), algorithm="ES256")
    show_token_info(token, claims)

def generate_broken_token():
    choices = {
        "a": ("expired", lambda: jwt.encode({
            "sub": "alice", "iat": datetime.now(timezone.utc) - timedelta(hours=1),
            "exp": datetime.now(timezone.utc) - timedelta(minutes=5),
            "iss": ISSUER, "aud": [AUDIENCE]}, load_private_key(), algorithm="ES256")),

        "b": ("long_ttl", lambda: jwt.encode({
            "sub": "bob", "iat": datetime.now(timezone.utc),
            "exp": datetime.now(timezone.utc) + timedelta(minutes=15),
            "iss": ISSUER, "aud": [AUDIENCE]}, load_private_key(), algorithm="ES256")),

        "c": ("invalid_subject", lambda: jwt.encode({
            "sub": "not-allowed", "iat": datetime.now(timezone.utc),
            "exp": datetime.now(timezone.utc) + timedelta(minutes=5),
            "iss": ISSUER, "aud": [AUDIENCE]}, load_private_key(), algorithm="ES256")),

        "d": ("bad_signature (signed with RS256 instead of ES256)", lambda: jwt.encode({
            "sub": "alice", "iat": datetime.now(timezone.utc),
            "exp": datetime.now(timezone.utc) + timedelta(minutes=5),
            "iss": ISSUER, "aud": [AUDIENCE]}, rsa.generate_private_key(public_exponent=65537, key_size=2048).private_bytes(
            serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8, serialization.NoEncryption()
        ), algorithm="RS256")),

        "e": ("parse_error", lambda: "invalid.token.payload"),

        "f": ("null_subject", lambda: jwt.encode({
            "sub": None, "iat": datetime.now(timezone.utc),
            "exp": datetime.now(timezone.utc) + timedelta(minutes=5),
            "iss": ISSUER, "aud": [AUDIENCE]}, load_private_key(), algorithm="ES256")),

        "g": ("blank_subject", lambda: jwt.encode({
            "sub": "  ", "iat": datetime.now(timezone.utc),
            "exp": datetime.now(timezone.utc) + timedelta(minutes=5),
            "iss": ISSUER, "aud": [AUDIENCE]}, load_private_key(), algorithm="ES256")),
    }

    console.print("\n[bold cyan]Choose broken token type:[/bold cyan]")
    for k, (label, _) in choices.items():
        console.print(f"[bold magenta]{k}[/bold magenta]: {label}")

    selected = Prompt.ask("Enter type", choices=list(choices.keys()), default="a")
    label, token_fn = choices[selected]
    token = token_fn()

    show_token_info(token, label)

def show_token_info(token, claims_or_reason):
    panel = Panel.fit(token, title="JWT Token", subtitle="Paste in Swagger > Authorize", style="cyan")
    console.print(panel)
    try:
        pyperclip.copy(token)
        console.print("[green]\U0001F4CB Token copied to clipboard![/green]")
    except Exception:
        console.print("[yellow]\u26A0\uFE0F Could not copy to clipboard[/yellow]")

    if isinstance(claims_or_reason, dict):
        table = Table(title="\U0001F4DC Token Claims", box=None)
        table.add_column("Claim", style="magenta")
        table.add_column("Value", style="white")
        for k in claims_or_reason:
            table.add_row(k, str(claims_or_reason[k]))
        console.print(table)
    else:
        console.print(f"[bold yellow]Token purpose:[/bold yellow] {claims_or_reason}")

    input("Press [Enter] to return to menu...")

def clean_existing_container(name):
    containers = subprocess.run(["docker", "ps", "-a", "--format", "{{.Names}}"], capture_output=True, text=True).stdout.splitlines()
    if name in containers:
        console.print(f"[yellow]⚠️ Removing existing container '{name}'[/yellow]")
        subprocess.run(["docker", "rm", "-f", name])

# def run_compose(file_path, project_name, message):
#     if not os.path.exists(file_path):
#         console.print(f"[red]❌ Missing Docker Compose file: {file_path}[/red]")
#         input("Press [Enter] to return to menu...")
#         return
#
#     container_names = [
#         "safe-access-mongo",
#         "safe-access-postgres1",
#         "safe-access-postgres2",
#         "safe-access-prometheus",
#         "safe-access-grafana",
#         "safe-access-app",
#     ]
#     for name in container_names:
#         clean_existing_container(name)
#
#     # Start in detached mode and suppress logs
#     subprocess.run([
#         "docker-compose", "-f", file_path, "-p", project_name,
#         "up", "-d", "--build"
#     ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
#
#     # Show individual loading bars per container
#     errors = []
#     with Progress(SpinnerColumn(), TextColumn("[progress.description]{task.description}")) as progress:
#         tasks = {name: progress.add_task(f"Starting {name}...", start=False) for name in container_names}
#
#         for name in container_names:
#             progress.start_task(tasks[name])
#             for _ in range(10):  # Retry for ~5 seconds total
#                 status = subprocess.run(
#                     ["docker", "inspect", "-f", "{{.State.Running}}", name],
#                     capture_output=True, text=True
#                 )
#                 if status.returncode == 0 and status.stdout.strip() == "true":
#                     break
#                 time.sleep(0.5)
#             else:
#                 errors.append(name)
#             progress.update(tasks[name], description=f"{name} ✅" if name not in errors else f"{name} ❌")
#             time.sleep(0.3)
#
#     if errors:
#         console.print(f"[red]❌ Failed to start: {', '.join(errors)}[/red]")
#     else:
#         console.print(Panel.fit(
#             f"✅ [bold green]{project_name} started successfully![/bold green]\n\n{message}",
#             title="🟢 All Services Ready", style="bold green"
#         ))
#     input("Press [Enter] to return to menu...")

def run_compose(file_path, project_name, message):
    if not os.path.exists(file_path):
        console.print(f"[red]❌ Missing Docker Compose file: {file_path}[/red]")
        input("Press [Enter] to return to menu...")
        return

    is_local_dev = project_name == "safe-access-dev"

    # full list but app is handled separately
    container_names = [
        "safe-access-mongo",
        "safe-access-postgres1",
        "safe-access-postgres2",
        "safe-access-prometheus",
        "safe-access-grafana"
    ]
    if not is_local_dev:
        container_names.append("safe-access-app")

    for name in container_names:
        clean_existing_container(name)

    subprocess.run(["docker-compose", "-f", file_path, "-p", project_name, "up", "-d", "--build"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    time.sleep(2)

    errors = []
    with Progress(SpinnerColumn(), TextColumn("[progress.description]{task.description}")) as progress:
        tasks = {name: progress.add_task(f"Starting {name}...", start=False) for name in container_names}
        for name in container_names:
            progress.start_task(tasks[name])
            for _ in range(10):
                result = subprocess.run(["docker", "inspect", "-f", "{{.State.Running}}", name],
                                        capture_output=True, text=True)
                if result.returncode == 0 and result.stdout.strip() == "true":
                    break
                time.sleep(0.5)
            else:
                errors.append(name)
            progress.update(tasks[name], description=f"{name} ✅" if name not in errors else f"{name} ❌")
            time.sleep(0.3)

    if is_local_dev:
        # local development: do not start app container
        console.print(Panel.fit(
            """[bold green]✅ Local environment is ready![/bold green]

[bold yellow]Next step:[/bold yellow] Launch the app manually in IntelliJ IDEA.

Required Spring profile: [bold cyan]dev[/bold cyan]

Set the following environment variables:
[bold]TEST1_DB_URL[/bold]       = jdbc:postgresql://localhost:5432/test1
[bold]TEST1_DB_USERNAME[/bold]   = admin
[bold]TEST1_DB_PASSWORD[/bold]   = admin
[bold]TEST2_DB_URL[/bold]       = jdbc:postgresql://localhost:5433/test2
[bold]TEST2_DB_USERNAME[/bold]   = admin
[bold]TEST2_DB_PASSWORD[/bold]   = admin
[bold]PROMETHEUS_USER[/bold]     = prometheus
[bold]PROMETHEUS_PASSWORD_HASH[/bold] = tXeBJWJtdUk7QOqeOfUre.IRbNJJxByeXcekAk0vEuNxrdnQaEMzS

📍 You can find these under: Run Configurations → Environment Variables
""",
            title="🛠️ Ready for Local Development", style="bold yellow"
        ))
    elif errors:
        console.print(f"[red]❌ Failed to start: {', '.join(errors)}[/red]")
    else:
        console.print(Panel.fit(
            f"[green]✅ {project_name} started successfully![/green]\n\n{message}",
            title="🟢 All Services Ready", style="bold green"
        ))

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
        console.print("[bold red]5.[/bold red] Generate Broken JWT Token")
        console.print("[bold cyan]6.[/bold cyan] Run Docker Compose (Dev Build)")
        console.print("[bold cyan]7.[/bold cyan] Run Docker Compose (Prebuilt Image)")
        console.print("[bold red]8.[/bold red] Stop Docker Compose (Dev)")
        console.print("[bold red]9.[/bold red] Stop Docker Compose (Prebuilt)")
        console.print("[bold magenta]10.[/bold magenta] Exit")

        choice = Prompt.ask("\n[?] Choose an option", choices=[str(i) for i in range(1, 11)], default="10")

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
            generate_broken_token()
        elif choice == "6":
            run_compose(COMPOSE_DEV, "safe-access-dev", "Run IntelliJ IDEA with profile: [bold cyan]dev[/bold cyan]")
        elif choice == "7":
            run_compose(COMPOSE_IMG, "safe-access-img", "Open in browser: [bold]http://127.0.0.1:8080/swagger-ui/index.html[/bold]")
        elif choice == "8":
            stop_compose("docker-compose.yaml", "safe-access-dev")
        elif choice == "9":
            stop_compose("docker-compose.image.yaml", "safe-access-img")
        elif choice == "10":
            console.print("\n\U0001F44B [bold green]Goodbye and good hacking![/bold green]")
            break

if __name__ == "__main__":
    menu()
