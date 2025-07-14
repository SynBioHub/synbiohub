#!/bin/bash
set -o errexit

# Save git revision to config before removing .git folder
echo "Saving git revision to configuration..."
node gitrevupdate.js

echo "Git revision saved successfully!"
