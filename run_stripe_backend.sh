#!/usr/bin/env bash

# Script chạy nhanh backend Stripe cho app TDTUMobileBanking
# Cách dùng:
#   chmod +x run_stripe_backend.sh
#   ./run_stripe_backend.sh
#
# Backend sẽ listen ở http://localhost:4242
# Emulator Android truy cập qua http://10.0.2.2:4242

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/stripe_backend"

if [ ! -d "$BACKEND_DIR" ]; then
  echo "Không tìm thấy thư mục stripe_backend ở $BACKEND_DIR"
  exit 1
fi

cd "$BACKEND_DIR"

echo "Cài dependencies Node (nếu cần)..."
if [ ! -d "node_modules" ]; then
  npm install
fi

echo "Khởi động Stripe backend ở http://localhost:4242 ..."
echo "Nhấn Ctrl+C để dừng."
node server.js


