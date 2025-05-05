#!/usr/bin/env python3
import os
import subprocess
from datetime import datetime, timedelta

import click
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import ec

import jwt

PRIVATE_KEY_PATH = "ec256-private.pem"
PUBLIC_KEY_DER_PATH = "ec256-public.der"
PUBLIC_KEY_PEM_PATH = "ec256-public.pem"

ISSUER = "dbaccess-api"
AUDIENCE = "dbaccess-client"
SUBJECTS = ["alice", "bob", "charlie"]

def generate_keys():
    print("🔐 Generating EC256 key pair...")
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

    print("✅ EC256 keys generated.")

def load_private_key():
    with open(PRIVATE_KEY_PATH, "rb") as key_file:
        return serialization.load_pem_private_key(
            key_file.read(),
            password=None,
            backend=default_backend()
        )

@click.group()
def cli():
    pass

@cli.command()
def install_deps():
    print("📦 Installing required Python packages...")
    subprocess.check_call(["pip", "install", "click", "pyjwt", "cryptography"])

@cli.command()
def show_keys():
    if not os.path.exists(PRIVATE_KEY_PATH):
        print("⚠️ No keys found.")
    else:
        print(f"📂 Private Key: {PRIVATE_KEY_PATH}")
        print(f"📂 Public Key (PEM): {PUBLIC_KEY_PEM_PATH}")
        print(f"📂 Public Key (DER): {PUBLIC_KEY_DER_PATH}")

@cli.command()
def generate_keys_if_missing():
    if not os.path.exists(PRIVATE_KEY_PATH):
        generate_keys()
    else:
        print("✅ Keys already exist.")

@cli.command()
@click.option("--subject", type=click.Choice(SUBJECTS), prompt="Choose subject (alice, bob, charlie)")
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

    print("✅ JWT Token Generated:")
    print(token)
    print()
    print("📜 Token Claims:")
    print(f"  • sub: {subject}")
    print(f"  • iat: {now.isoformat()} UTC")
    print(f"  • exp: {exp.isoformat()} UTC")
    print(f"  • iss: {ISSUER}")
    print(f"  • aud: {AUDIENCE}")
    print("🔒 Signed using ECDSA (P-256)")

@cli.command()
def run_dev_compose():
    print("🐳 Running Docker Compose for local dev...")
    os.system("docker-compose -f docker-compose.dev.yml up --build")

@cli.command()
def run_image_compose():
    print("🐳 Running Docker Compose with prebuilt image...")
    os.system("docker-compose -f docker-compose.image.yml up")

@cli.command()
def menu():
    while True:
        click.clear()
        print("""
========= 🧪 Safe Temporary DB Access – CLI =========
1️⃣  Install Python Dependencies
2️⃣  Generate EC256 Keys (if missing)
3️⃣  Show Key Paths
4️⃣  Generate JWT Token
5️⃣  Run Docker Compose (Dev Build)
6️⃣  Run Docker Compose (Prebuilt Image)
7️⃣  Exit
=====================================================""")

        choice = input("Select option (1-7): ").strip()
        if choice == "1":
            install_deps()
        elif choice == "2":
            generate_keys_if_missing()
        elif choice == "3":
            show_keys()
        elif choice == "4":
            generate_token()
        elif choice == "5":
            run_dev_compose()
        elif choice == "6":
            run_image_compose()
        elif choice == "7":
            print("👋 Goodbye!")
            break
        else:
            print("❌ Invalid choice. Try again.")
        input("\nPress Enter to continue...")

if __name__ == "__main__":
    cli()