#!/usr/bin/env python
import os
import subprocess
import sys
from datetime import datetime, timedelta

# Force UTF-8 output encoding
os.environ["PYTHONIOENCODING"] = "utf-8"

try:
    import jwt
    import click
    from rich.console import Console
    from rich.table import Table
    from rich.panel import Panel
    from rich.prompt import Prompt
    from rich.text import Text
    from rich import box
except ModuleNotFoundError:
    print("\n📦 Required modules not found. Installing them now...\n")
    try:
        subprocess.check_call([sys.executable, "-m", "pip", "install", "click", "pyjwt", "cryptography", "rich"])
        os.execv(sys.executable, [sys.executable] + sys.argv)
    except subprocess.CalledProcessError as e:
        print(f"❌ Installation failed: {e}")
        sys.exit(1)

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec

BASE_DIR = os.path.dirname(__file__)
KEY_DIR = os.path.join(BASE_DIR, "jwt")
PRIVATE_KEY_PATH = os.path.join(KEY_DIR, "ec256-private.pem")
PUBLIC_KEY_DER_PATH = os.path.join(KEY_DIR, "ec256-public.der")
PUBLIC_KEY_PEM_PATH = os.path.join(KEY_DIR, "ec256-public.pem")

ISSUER = "dbaccess-api"
AUDIENCE = "dbaccess-client"
SUBJECTS = ["alice", "bob", "charlie"]

console = Console()

ASCII_HEADER = """
[1;35m
███████╗██╗   ██╗██████╗ ███████╗██████╗ ██╗   ██╗███╗   ██╗██╗  ██╗
██╔════╝██║   ██║██╔══██╗██╔════╝██╔══██╗██║   ██║████╗  ██║██║ ██╔╝
███████╗██║   ██║██████╔╝█████╗  ██████╔╝██║   ██║██╔██╗ ██║█████╔╝ 
╚════██║██║   ██║██╔═══╝ ██╔══╝  ██╔═══╝ ██║   ██║██║╚██╗██║██╔═██╗ 
███████║╚██████╔╝██║     ███████╗██║     ╚██████╔╝██║ ╚████║██║  ██╗
╚══════╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝      ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝
"""

COPYRIGHT = Text("Safe Temporary DB Access © 2024 - https://github.com/CamilYed/safe-temporary-db-access", style="dim")

def ensure_key_dir():
    os.makedirs(KEY_DIR, exist_ok=True)

def generate_keys():
    ensure_key_dir()
    console.print("\n[bold magenta]🔐 Generating EC256 key pair...[/bold magenta]")
    private_key = ec.generate_private_key(ec.SECP256R1(), default_backend())

    with open(PRIVATE_KEY_PATH, "wb") as f:
        f.write(private_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption()
        ))

    pub_key = private_key.public_key()

    with open(PUBLIC_KEY_PEM_PATH, "wb") as f:
        f.write(pub_key.public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        ))

    with open(PUBLIC_KEY_DER_PATH, "wb") as f:
        f.write(pub_key.public_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        ))

    console.print("[bold green]✅ EC256 keys generated.[/bold green]\n")
    input("Press [Enter] to return to menu...")

def load_private_key():
    with open(PRIVATE_KEY_PATH, "rb") as key_file:
        return serialization.load_pem_private_key(
            key_file.read(),
            password=None,
            backend=default_backend()
        )

def generate_token(subject):
    if not os.path.exists(PRIVATE_KEY_PATH):
        generate_keys()

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

    console.print("\n[bold cyan]✅ JWT Token Generated[/bold cyan]")
    console.print(Panel(token, title="🔐 Encrypted Token", style="bold green", expand=False))

    claim_table = Table(title="📄 Claims", box=box.ROUNDED)
    claim_table.add_column("Claim", style="bright_magenta")
    claim_table.add_column("Value", style="white")
    for k, v in claims.items():
        claim_table.add_row(k, str(v))

    console.print(claim_table)
    input("\nPress [Enter] to return to menu...")

def run_dev_compose():
    console.print("\n📣 [bold]Running Docker Compose for local dev...[/bold]")
    path = os.path.join(BASE_DIR, "docker/docker-compose.yml")
    subprocess.run(["docker-compose", "-f", path, "up", "--build"], check=True)
    input("\nPress [Enter] to return to menu...")

def run_image_compose():
    console.print("\n📣 [bold]Running Docker Compose with prebuilt image...[/bold]")
    path = os.path.join(BASE_DIR, "docker/docker-compose.image.yml")
    subprocess.run(["docker-compose", "-f", path, "up", "--build"], check=True)
    subprocess.run(["docker-compose", "ps"], check=True)
    input("\nPress [Enter] to return to menu...")

def menu():
    while True:
        console.clear()
        console.print(Panel(ASCII_HEADER, style="bold magenta"))
        console.print(COPYRIGHT)
        console.rule("🧪 [bold blue]Cyber Access Terminal[/bold blue]", style="blue")
        console.print("[bold cyan]1.[/bold cyan] Install Python Dependencies")
        console.print("[bold cyan]2.[/bold cyan] Generate EC256 Keys (if missing)")
        console.print("[bold cyan]3.[/bold cyan] Show Key Paths")
        console.print("[bold cyan]4.[/bold cyan] Generate JWT Token")
        console.print("[bold cyan]5.[/bold cyan] Run Docker Compose (Dev Build)")
        console.print("[bold cyan]6.[/bold cyan] Run Docker Compose (Prebuilt Image)")
        console.print("[bold red]7.[/bold red] Exit")
        console.rule(style="blue")

        choice = Prompt.ask("\n[?] Choose an option", choices=["1", "2", "3", "4", "5", "6", "7"], default="7")

        try:
            if choice == "1":
                console.print("\n📦 Installing required Python packages...")
                subprocess.check_call(["pip", "install", "-r", os.path.join(BASE_DIR, "requirements.txt")])
                input("\nPress [Enter] to return to menu...")
            elif choice == "2":
                if not os.path.exists(PRIVATE_KEY_PATH):
                    generate_keys()
                else:
                    console.print("[green]✅ Keys already exist.[/green]")
                    input("\nPress [Enter] to return to menu...")
            elif choice == "3":
                if not os.path.exists(PRIVATE_KEY_PATH):
                    console.print("[red]⚠ Keys not found.[/red]")
                else:
                    table = Table(title="🔑 Key Paths", box=box.SQUARE)
                    table.add_column("Type", style="cyan", no_wrap=True)
                    table.add_column("Path", style="green")
                    table.add_row("Private Key", PRIVATE_KEY_PATH)
                    table.add_row("Public Key (PEM)", PUBLIC_KEY_PEM_PATH)
                    table.add_row("Public Key (DER)", PUBLIC_KEY_DER_PATH)
                    console.print(table)
                    input("\nPress [Enter] to return to menu...")
            elif choice == "4":
                subject = Prompt.ask("Enter subject", choices=SUBJECTS)
                generate_token(subject)
            elif choice == "5":
                run_dev_compose()
            elif choice == "6":
                run_image_compose()
            elif choice == "7":
                console.print("\n👋 [bold green]Goodbye, hacker.[/bold green]")
                break
        except subprocess.CalledProcessError as e:
            console.print(f"[red]❌ Error running command: {e}[/red]")

if __name__ == "__main__":
    menu()