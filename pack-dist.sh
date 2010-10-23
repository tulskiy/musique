#!/bin/sh
cd dist/musique

if [ -z $1 ]; then
    echo "Usage: pack-dist.sh VERSION"
    exit
fi

for i in *.jar lib/*.jar
do
	pack200 -r -G $i
done

cd ..
zip musique-$1-binary.zip -9 -r musique