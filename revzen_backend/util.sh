POSTGRES_URL='postgres://xehjgjemxkoevv:56bc4268ea9ec1907ef226cd9bfe46a6490fb87491a3431c5c693fdffd681c2f@ec2-34-231-221-151.compute-1.amazonaws.com:5432/d9j2nr4kjidcog'
POSTGRES_TIMEOUT=10
POSTGRES_CONNS=5

help='
This is the basic backend utility
-h  Display this help text!
-r  Run the server locally (debug) connecting to the database as specified by the variables in util.sh
-c  Check the repo will past test, lint and format locally before running the gitlab pipeline
-d  Build, then open the documentation in the browser.
'

check() {
    echo "Checking the repo will pass basic CI"
    echo "TESTING..."
    cargo test
    echo "LINTING..."
    cargo clippy
    echo "FORMATTING..."
    cargo fmt
}

run() {
    echo "Running the server on localhost, connected to the dev heroku postgres database."
    cargo build;
    sensible-browser 'http://127.0.0.1:8000'
    ROCKET_DATABASES="{revzen_db={url=\"$POSTGRES_URL\", timeout=$POSTGRES_TIMEOUT, pool_size=$POSTGRES_CONNS}}" ./target/debug/revzen_backend
}

doc() {
    echo "Building and then launching the documentation"
    cargo doc;
    sensible-browser 'target/doc/revzen_backend/index.html'
}

while getopts hcrd flag;
do
    case "${flag}" in
        h) echo "$help"; exit;;
        c) check;;
        r) run;;
        d) doc;;
    esac
done