#!/bin/bash

# ==============================================
# Let's Encrypt SSL 인증서 초기 발급 스크립트
# 서버에서 최초 1회만 실행하면 됩니다.
# 사용법: sudo bash init-letsencrypt.sh
# ==============================================

DOMAIN="opencloset.jihongeek.com"
EMAIL="opencloset2026@gmail.com"  # Let's Encrypt 알림용 이메일

# 1. 기존 인증서 디렉토리 생성
echo ">>> 디렉토리 생성..."
mkdir -p ./certbot/conf
mkdir -p ./certbot/www

# 2. 임시 자체 서명 인증서 생성 (Nginx가 SSL 설정으로 시작할 수 있도록)
echo ">>> 임시 SSL 인증서 생성..."
mkdir -p ./certbot/conf/live/$DOMAIN
openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
  -keyout ./certbot/conf/live/$DOMAIN/privkey.pem \
  -out ./certbot/conf/live/$DOMAIN/fullchain.pem \
  -subj "/CN=$DOMAIN"

# 3. Nginx 시작 (임시 인증서로)
echo ">>> Nginx 시작..."
docker compose up -d nginx

# 4. 임시 인증서 삭제
echo ">>> 임시 인증서 삭제..."
rm -rf ./certbot/conf/live/$DOMAIN

# 5. 실제 Let's Encrypt 인증서 발급
echo ">>> Let's Encrypt 인증서 발급 중..."
docker compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email $EMAIL \
  --agree-tos \
  --no-eff-email \
  -d $DOMAIN

# 6. Nginx 재시작 (실제 인증서 적용)
echo ">>> Nginx 재시작..."
docker compose restart nginx

echo ">>> 완료! https://$DOMAIN 으로 접속해보세요."
