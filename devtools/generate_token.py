import os
import subprocess
from datetime import datetime, timedelta

try:
    import jwt
    import click
    from rich.console import Console
    from rich.table import Table
    from rich.panel import Panel
    from rich.prompt import Prompt, Confirm
except ModuleNotFoundError:
    print("📦 Required modules not found. Installing them now...")
    subprocess.check_call(["pip", "install", "click", "pyjwt", "cryptography", "rich"])
    import jwt
    import click
    from rich.console import Console
    from rich.table import Table
    from rich.panel import Panel
    from rich.prompt import Prompt, Confirm

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec

KEY_DIR = os.path.join(".", "jwt")
PRIVATE_KEY_PATH = os.path.join(KEY_DIR, "ec256-private.pem")
PUBLIC_KEY_DER_PATH = os.path.join(KEY_DIR, "ec256-public.der")
PUBLIC_KEY_PEM_PATH = os.path.join(KEY_DIR, "ec256-public.pem")

ISSUER = "dbaccess-api"
AUDIENCE = "dbaccess-client"
SUBJECTS = ["alice", "bob", "charlie"]

console = Console()

def ensure_key_dir():
    os.makedirs(KEY_DIR, exist_ok=True)

def generate_keys():
    ensure_key_dir()
    console.print("\n[bold cyan]🔐 Generating EC256 key pair...[/bold cyan]")
    private_key = ec.generate_private_key(ec.SECP256R1(), default_backend())

    pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.TraditionalOpenSSL,
        encryption_algorithm=serialization.NoEncryption()
    )

    with open(PRIVATE_KEY_PATH, "wb") as f:
        f.write(pem)

    pub_key = private_key.public_key()

    pub_pem = pub_key.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo
    )

    pub_der = pub_key.public_bytes(
        encoding=serialization.Encoding.DER,
        format=serialization.PublicFormat.SubjectPublicKeyInfo
    )

    with open(PUBLIC_KEY_PEM_PATH, "wb") as f:
        f.write(pub_pem)

    with open(PUBLIC_KEY_DER_PATH, "wb") as f:
        f.write(pub_der)

    console.print("[green]✅ EC256 keys generated successfully.[/green]\n")

def load_private_key():
    with open(PRIVATE_KEY_PATH, "rb") as key_file:
        return serialization.load_pem_private_key(
            key_file.read(),
            password=None,
            backend=default_backend()
        )

def install_deps():
    console.print("\n📦 Installing required Python packages...")
    subprocess.check_call(["pip", "install", "-r", "devtools/requirements.txt"])

def show_keys():
    if not os.path.exists(PRIVATE_KEY_PATH):
        console.print("[red]⚠️ No keys found.[/red]")
    else:
        table = Table(title="🔑 Key Paths", show_lines=True)
        table.add_column("Type", style="cyan", no_wrap=True)
        table.add_column("Path", style="green")
        table.add_row("Private Key", PRIVATE_KEY_PATH)
        table.add_row("Public Key (PEM)", PUBLIC_KEY_PEM_PATH)
        table.add_row("Public Key (DER)", PUBLIC_KEY_DER_PATH)
        console.print(table)

def generate_keys_if_missing():
    if not os.path.exists(PRIVATE_KEY_PATH):
        generate_keys()
    else:
        console.print("[green]✅ Keys already exist.[/green]")

def generate_token(subject):
    generate_keys_if_missing()
    private_key = load_private_key()
    now = datetime.utcnow()
    exp = now + timedelta(minutes=5)

    claims = {
        "sub": subject,
        "iat": now,
        "exp": exp,
        "iss": ISSUER,
        "aud": [AUDIENCE]
    }

    token = jwt.encode(claims, private_key, algorithm="ES256")

    console.print("\n[bold green]✅ JWT Token Generated[/bold green]")
    console.print(Panel(token, title="🔐 Token", subtitle="Use this for API access", expand=False))

    claim_table = Table(title="📜 Token Claims")
    claim_table.add_column("Claim", style="cyan", no_wrap=True)
    claim_table.add_column("Value", style="white")
    claim_table.add_row("sub", subject)
    claim_table.add_row("iat", now.isoformat() + " UTC")
    claim_table.add_row("exp", exp.isoformat() + " UTC")
    claim_table.add_row("iss", ISSUER)
    claim_table.add_row("aud", AUDIENCE)

    console.print(claim_table)

def run_dev_compose():
    console.print("\n🐳 [bold]Running Docker Compose for local dev...[/bold]")
    subprocess.run(["docker-compose", "-f", "devtools/docker/docker-compose.yml", "up", "--build"], check=True)

def run_image_compose():
    console.print("\n🐳 [bold]Running Docker Compose with prebuilt image...[/bold]")
    subprocess.run(["docker-compose", "-f", "devtools/docker/docker-compose.image.yml", "up"], check=True)
    subprocess.run(["docker-compose", "ps"], check=True)

def menu():
    while True:
        console.clear()
        console.rule("🧪 [bold blue]Safe Temporary DB Access – CLI[/bold blue]", style="blue")
        console.print("[bold]1.[/bold] Install Python Dependencies")
        console.print("[bold]2.[/bold] Generate EC256 Keys (if missing)")
        console.print("[bold]3.[/bold] Show Key Paths")
        console.print("[bold]4.[/bold] Generate JWT Token")
        console.print("[bold]5.[/bold] Run Docker Compose (Dev Build)")
        console.print("[bold]6.[/bold] Run Docker Compose (Prebuilt Image)")
        console.print("[bold red]7.[/bold red] Exit")
        console.rule(style="blue")

        choice = Prompt.ask("\n[?] Choose an option", choices=["1", "2", "3", "4", "5", "6", "7"], default="7")

        try:
            if choice == "1":
                install_deps()
            elif choice == "2":
                generate_keys_if_missing()
            elif choice == "3":
                show_keys()
            elif choice == "4":
                subject = Prompt.ask("Enter subject", choices=SUBJECTS)
                generate_token(subject)
            elif choice == "5":
                run_dev_compose()
            elif choice == "6":
                run_image_compose()
            elif choice == "7":
                console.print("\n👋 [bold green]Goodbye![/bold green]")
                break
        except subprocess.CalledProcessError as e:
            console.print(f"[red]❌ Error running command: {e}[/red]")

        if Confirm.ask("\n🔁 Return to menu?") is False:
            break

if __name__ == "__main__":
    menu()
