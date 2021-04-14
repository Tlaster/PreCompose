echo $SIGNING_KEY | base64 -d > ./precompose/key.gpg
echo "signing.keyId=$SIGNING_KEY_ID
signing.password=$SIGNING_PASSWORD
signing.secretKeyRingFile=./key.gpg
ossrhUsername=$OSSRH_USERNAME
ossrhPassword=$OSSRH_PASSWORD" >publish.properties