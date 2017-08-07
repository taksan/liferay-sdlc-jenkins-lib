Jenkins is pointing to a specific tag version of this lib (e.g. v1). So, after modifying master, you should create a new tag and change Jenkins to use it:

- Configure System > Global Pipeline Libraries > Default version

