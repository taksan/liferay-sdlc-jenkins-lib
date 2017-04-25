def call(where, commands)
{
    sh "ssh -o BatchMode=yes -o StrictHostKeyChecking=no $where \"$commands\""
}
