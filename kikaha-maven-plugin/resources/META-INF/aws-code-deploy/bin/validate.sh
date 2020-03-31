#!/bin/sh

check_connection(){
    {{validation-command}}
}

wait_to_start() {
    while true
    do
        if check_connection; then
            echo "Server ONLINE!"
            break
        fi
        echo "Server OFFLINE, waiting for start..."
        sleep 2
    done
}

wait_to_start