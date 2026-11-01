#!/usr/bin/env bash
# Copies the frontend into docs/ for GitHub Pages and points it at your hosted backend.
# Usage: bash scripts/build-pages.sh https://your-app.onrender.com
set -e
API="${1:?Please give your backend URL, e.g. https://your-app.onrender.com}"
rm -rf docs && mkdir docs
cp -r src/main/resources/static/* docs/
echo "window.HV_API_BASE = \"${API%/}\";" > docs/js/config.js
touch docs/.nojekyll
echo "docs/ is ready. Commit and push it, then enable GitHub Pages (branch main, folder /docs)."
