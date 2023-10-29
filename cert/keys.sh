#!/usr/bin/env bash

DNAME="CN=Binwei, OU=IES LUIS VIVES, O=IES LUIS VIVES, L=LÉGANES, S=MADRID, C=ES"

## Llavero Servidor: Par de claves del servidor
keytool -genkeypair -alias serverKeyPair -keyalg RSA -keysize 2048 -validity 365 -storetype PKCS12 -keystore server_keystore.p12 -storepass 1234567 -dname "$DNAME"

## Certificado del servidor, exportamos
keytool -exportcert -alias serverKeyPair -storetype PKCS12 -keystore server_keystore.p12 -file server_certificate.cer -rfc -storepass 1234567

## Llavero Cliente: Importamos el certificado del servidor
keytool -importcert -alias clientKeyPair -storetype PKCS12 -keystore client_keystore.p12 -file server_certificate.cer -rfc -storepass 1234567
