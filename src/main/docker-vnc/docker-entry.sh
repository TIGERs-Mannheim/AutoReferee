#!/usr/bin/env bash

args="$@"

if [[ "$1" == "vnc" ]]; then
  echo "Launch in VNC mode"
  args="${@:1}"
  if [[ -z "${VNC_PASSWORD}" ]]; then
    VNC_PASSWORD=vncpassword
  fi
  if [[ -z "${VNC_GEOMETRY}" ]]; then
    VNC_GEOMETRY=1280x1024
  fi

  mkdir ~/.vnc
  x11vnc -storepasswd "${VNC_PASSWORD}" ~/.vnc/passwd

  cat <<EOF >>~/.xinitrc
#!/bin/sh
exec java -cp "/app/resources:/app/classes:/app/libs/*" "edu.tigers.autoref.AutoReferee" -w "${VNC_GEOMETRY}" $args > /tmp/out.log
EOF
  chmod 700 ~/.xinitrc
  
  touch /tmp/out.log
  tail -f /tmp/out.log &

  export X11VNC_CREATE_GEOM="${VNC_GEOMETRY}"
  exec x11vnc -forever -usepw -display WAIT:cmd=FINDCREATEDISPLAY-Xvfb
else
  echo "Launch in headless mode"
  exec java -cp "/app/resources:/app/classes:/app/libs/*" "edu.tigers.autoref.AutoReferee" -hl $args
fi
