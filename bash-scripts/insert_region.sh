#!/bin/bash

country_code=$1
from_lvl=$2
to_lvl=$3
file_name=$4
api_key="2b4dbf5a-182f-4fe6-857a-f9e213b73735"

url="https://wambachers-osm.website/boundaries/exportBoundaries?cliVersion=1.0&cliKey="$api_key"&exportFormat=json&exportLayout=levels&exportAreas=land&union=false&from_AL="$from_lvl"&to_AL="$to_lvl"&selected="$country_code
echo $url

full_path="../osmdata/"

curl -f -o $full_path$file_name".zip" --url $url

unzip $full_path$file_name".zip" -d $full_path$file_name
