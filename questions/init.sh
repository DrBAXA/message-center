#!/bin/bash
export DATABASE_SERVER="$(echo $MYDB_PORT | grep -Po '(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d{4,5})')"
bash