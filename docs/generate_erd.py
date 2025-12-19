#!/usr/bin/env python3
"""
Script để generate ERD diagram từ các model trong TDTU Mobile Banking app
Sử dụng Mermaid syntax để vẽ Entity-Relationship Diagram
"""

import re
import os
from pathlib import Path
from typing import Dict, List, Tuple, Optional

# Định nghĩa các model và collection
MODELS = {
    "User": {
        "collection": "users",
        "primary_key": "uid",
        "fields": [
            ("uid", "string", "PK"),
            ("fullName", "string"),
            ("email", "string"),
            ("role", "string"),
            ("phoneNumber", "string"),
            ("kycStatus", "enum (VERIFIED, PENDING, NONE)"),
            ("avatarUrl", "string")
        ]
    },
    "Account": {
        "collection": "accounts",
        "primary_key": "accountId",
        "fields": [
            ("accountId", "string", "PK"),
            ("ownerId", "string", "FK -> User.uid"),
            ("accountType", "enum (CHECKING, SAVING, MORTGAGE)"),
            ("balance", "double"),
            ("currency", "string"),
            ("interestRate", "double", "nullable"),
            ("termMonth", "int", "nullable"),
            ("principalAmount", "double", "nullable"),
            ("mortgageRate", "double", "nullable"),
            ("termMonths", "int", "nullable"),
            ("startDate", "timestamp", "nullable")
        ]
    },
    "Transaction": {
        "collection": "transactions",
        "primary_key": "transactionId",
        "fields": [
            ("transactionId", "string", "PK"),
            ("senderAccountId", "string", "FK -> Account.accountId"),
            ("receiverAccountId", "string", "FK -> Account.accountId"),
            ("amount", "double"),
            ("type", "enum (TRANSFER_INTERNAL, TRANSFER_EXTERNAL, BILL_PAYMENT, DEPOSIT, WITHDRAWAL)"),
            ("status", "enum (SUCCESS, FAILED)"),
            ("timestamp", "timestamp"),
            ("description", "string")
        ]
    },
    "Bill": {
        "collection": "bills",
        "primary_key": "billId",
        "fields": [
            ("billId", "string", "PK"),
            ("billCode", "string"),
            ("billType", "string"),
            ("customerName", "string"),
            ("customerCode", "string"),
            ("provider", "string"),
            ("amount", "double"),
            ("status", "enum (UNPAID, PAID, OVERDUE, CANCELLED)"),
            ("dueDate", "timestamp"),
            ("createdAt", "timestamp"),
            ("paidAt", "timestamp", "nullable"),
            ("description", "string")
        ]
    },
    "Branch": {
        "collection": "branches",
        "primary_key": "branchId",
        "fields": [
            ("branchId", "string", "PK"),
            ("name", "string"),
            ("latitude", "double"),
            ("longitude", "double"),
            ("address", "string")
        ]
    }
}

# Relationships
# Đảo ngược một số relationships để Transaction ở trung tâm
RELATIONSHIPS = [
    ("User", "uid", "Account", "ownerId", "has", "one-to-many"),
    # Gộp hai mối quan hệ sends và receives thành một
    ("Transaction", "senderAccountId", "Account", "accountId", "sends/receives", "many-to-one"),
]


def get_field_type_display(field_name: str, field_type: str, constraints: str = "") -> str:
    """Format field type for display"""
    # Mermaid ERD doesn't support enum with parentheses, simplify enum types
    if field_type.startswith("enum"):
        # Extract enum values and simplify
        enum_match = re.search(r"enum \((.+)\)", field_type)
        if enum_match:
            enum_values = enum_match.group(1)
            # Use first few values or simplify
            if len(enum_values) > 30:
                display_type = "string"
            else:
                display_type = "string"  # Mermaid doesn't support enum syntax well
        else:
            display_type = "string"
    else:
        display_type = field_type
    
    # Mermaid ERD doesn't support ? for nullable, use "nullable" as suffix in comment
    # We'll just use the base type without ? marker
    return display_type


def generate_mermaid_erd() -> str:
    """Generate Mermaid ERD syntax"""
    lines = ["erDiagram"]
    lines.append("")
    
    # Sắp xếp thứ tự: Đặt Transaction ở giữa danh sách
    # Mermaid thường đặt entity ở giữa danh sách ở vị trí trung tâm hơn
    entity_order = ["User", "Account", "Transaction", "Bill", "Branch"]
    
    # Generate entities with fields theo thứ tự đã định
    for model_name in entity_order:
        if model_name not in MODELS:
            continue
        model_info = MODELS[model_name]
        lines.append(f"    {model_name} {{")
        for field_name, field_type, *constraints in model_info["fields"]:
            constraint_str = " ".join(constraints) if constraints else ""
            
            # Get base type (simplify enum types for Mermaid)
            base_type = get_field_type_display(field_name, field_type, constraint_str)
            
            # Add PK/FK markers
            if "PK" in constraint_str:
                field_display = f"{base_type} {field_name} PK"
            elif "FK" in constraint_str:
                field_display = f"{base_type} {field_name} FK"
            else:
                field_display = f"{base_type} {field_name}"
            
            lines.append(f"        {field_display}")
        lines.append("    }")
        lines.append("")
    
    # Generate relationships
    # Đặt relationships liên quan đến Transaction ở giữa danh sách
    # Sắp xếp: User->Account trước, Transaction relationships giữa
    sorted_relationships = sorted(RELATIONSHIPS, key=lambda x: (
        "Transaction" in (x[0], x[2])  # Transaction relationships ở giữa
    ))
    
    for from_table, from_field, to_table, to_field, relation_name, relation_type in sorted_relationships:
        if relation_type == "one-to-many":
            lines.append(f"    {from_table} ||--o{{ {to_table} : \"{relation_name}\"")
        elif relation_type == "many-to-one":
            lines.append(f"    {from_table} }}o--|| {to_table} : \"{relation_name}\"")
        elif relation_type == "one-to-one":
            lines.append(f"    {from_table} ||--|| {to_table} : \"{relation_name}\"")
        else:
            lines.append(f"    {from_table} }}o--o{{ {to_table} : \"{relation_name}\"")
    
    return "\n".join(lines)


def generate_markdown_documentation() -> str:
    """Generate markdown documentation with ERD"""
    md_lines = [
        "# TDTU Mobile Banking - Entity Relationship Diagram",
        "",
        "## Database Schema",
        "",
        "Ứng dụng sử dụng Firebase Firestore làm database. Dưới đây là cấu trúc các collection và mối quan hệ giữa chúng.",
        "",
        "## ERD Diagram",
        "",
        "```mermaid"
    ]
    
    md_lines.append(generate_mermaid_erd())
    md_lines.append("```")
    md_lines.append("")
    md_lines.append("## Collections")
    md_lines.append("")
    
    # Add collection details
    for model_name, model_info in MODELS.items():
        md_lines.append(f"### {model_name} (Collection: `{model_info['collection']}`)")
        md_lines.append("")
        md_lines.append("| Field | Type | Constraints |")
        md_lines.append("|-------|------|-------------|")
        
        for field_name, field_type, *constraints in model_info["fields"]:
            constraint_str = " ".join(constraints) if constraints else "-"
            md_lines.append(f"| `{field_name}` | `{field_type}` | {constraint_str} |")
        
        md_lines.append("")
    
    md_lines.append("## Relationships")
    md_lines.append("")
    
    for from_table, from_field, to_table, to_field, relation_name, relation_type in RELATIONSHIPS:
        md_lines.append(f"- **{from_table}.{from_field}** → **{to_table}.{to_field}** ({relation_type})")
        md_lines.append(f"  - Relationship: {relation_name}")
        md_lines.append("")
    
    return "\n".join(md_lines)


def main():
    """Main function"""
    script_dir = Path(__file__).parent
    # File is now in docs directory, so use script_dir directly
    docs_dir = script_dir
    docs_dir.mkdir(exist_ok=True)
    
    # Generate Mermaid ERD file
    erd_file = docs_dir / "erd.mmd"
    mermaid_content = generate_mermaid_erd()
    
    with open(erd_file, "w", encoding="utf-8") as f:
        f.write(mermaid_content)
    
    print(f"✅ Đã tạo file ERD Mermaid: {erd_file}")
    
    # Generate Markdown documentation
    md_file = docs_dir / "erd.md"
    md_content = generate_markdown_documentation()
    
    with open(md_file, "w", encoding="utf-8") as f:
        f.write(md_content)
    
    print(f"✅ Đã tạo file documentation: {md_file}")
    
    # Render ERD thành PNG và SVG
    import subprocess
    import sys
    
    png_file = docs_dir / "erd.png"
    svg_file = docs_dir / "erd.svg"
    
    try:
        # Render PNG
        print(f"\n🖼️  Đang render PNG...")
        result = subprocess.run(
            ["mmdc", "-i", str(erd_file), "-o", str(png_file)],
            capture_output=True,
            text=True
        )
        if result.returncode == 0:
            print(f"✅ Đã tạo file PNG: {png_file}")
        else:
            print(f"⚠️  Lỗi khi render PNG: {result.stderr}")
        
        # Render SVG (độ phân giải cao)
        print(f"\n🖼️  Đang render SVG (độ phân giải cao)...")
        result = subprocess.run(
            ["mmdc", "-i", str(erd_file), "-o", str(svg_file)],
            capture_output=True,
            text=True
        )
        if result.returncode == 0:
            print(f"✅ Đã tạo file SVG: {svg_file}")
        else:
            print(f"⚠️  Lỗi khi render SVG: {result.stderr}")
    except FileNotFoundError:
        print("\n⚠️  Không tìm thấy mermaid-cli (mmdc)")
        print("   Vui lòng cài đặt: npm install -g @mermaid-js/mermaid-cli")
        print("\n   Sau đó chạy lệnh:")
        print(f"   mmdc -i {erd_file} -o {png_file}")
        print(f"   mmdc -i {erd_file} -o {svg_file} -f svg")
    
    # Print instructions
    print("\n" + "="*60)
    print("HƯỚNG DẪN SỬ DỤNG:")
    print("="*60)
    print("\n1. Xem ERD diagram:")
    print(f"   - Mở file: {md_file}")
    print(f"   - PNG: {png_file}")
    print(f"   - SVG (độ phân giải cao): {svg_file}")
    print("   - Hoặc xem trực tiếp trên GitHub (nếu đã push)")
    print("\n2. Render lại ERD:")
    print(f"   python3 generate_erd.py")
    print("\n3. Hoặc sử dụng online Mermaid editor:")
    print("   https://mermaid.live/")
    print("="*60)


if __name__ == "__main__":
    main()

