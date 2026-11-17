#!/usr/bin/env bash
# Loads ../.env then starts the backend
set -a; [ -f ../.env ] && . ../.env; set +a
mvn spring-boot:run
