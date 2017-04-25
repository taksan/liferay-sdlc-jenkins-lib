def call(files, destination) {
    sh "scp -o BatchMode=yes -o StrictHostKeyChecking=no $files $destination"
}
