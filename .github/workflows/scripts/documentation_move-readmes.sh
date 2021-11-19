#!/bin/bash
# NOTE : Quote it else use array to avoid problems #
mkdir generated
FILES="apps/**/README.md"
for f in $FILES
do
  echo "Processing $f file..."
  # take action on each file. $f store current file name

  echo "generated/$(dirname "$f")" | xargs mkdir -p &&  cp "$f" generated/"$f"
done

cp -R docs/* generated/
