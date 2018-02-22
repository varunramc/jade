#!/bin/bash

set -e

function appendToFileIfNotExists ()
{
    args=( "$@" )
    file=${args[0]}

    if [ ! -f "$file" ]
    then
        echo "File was not found and will be created: $file"
    fi

    for i in `seq 1 $(( ${#args[@]} - 1 ))`
    do
        line="${args[i]}"

        if ! grep -Fxiq "$line" "$file"
        then
            printf "\n$line\n" >> "$file"
        fi
    done
}

echo "--- Jade development environment setup ---"

# Determine the project root
projectRoot=`dirname $(dirname $(pwd))`

if [ -d "$projectRoot/tools" ]
then
    echo "Found project root at: $projectRoot"
else
    echo "Failed to find the project root at '$projectRoot'. Are you running the script from its dir?"
    exit 1
fi

echo '>>> Setting up environment variables'
appendToFileIfNotExists "$HOME/.bashrc" "export JADE_ROOT=$projectRoot"
appendToFileIfNotExists "$HOME/.bash_profile" "export JADE_ROOT=$projectRoot"

echo "Environment setup is complete"
echo "*** NOTE: Log out and log back in for the changes to take effect ***"

