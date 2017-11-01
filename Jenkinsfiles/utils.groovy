/*
 * Define utility functions.
 */

def get_target() {
    if (env.BRANCH_NAME == 'prod-push') {
        return 'prod'
    if (env.BRANCH_NAME == 'stage-push') {
        return 'stage'
    }
    return null
}

def get_repo() {
    echo sh(returnStdout: true, script: 'env')
    if (env.JOB_NAME.startsWith('mdn_multibranch')) {
        return 'kuma'
    if (env.JOB_NAME.startsWith('kumascript_multibranch')) {
        return 'kumascript'
    }
    return null
}

def checkout_github(repo, branch, relative_target_dir) {
    checkout(
        [$class: 'GitSCM',
         userRemoteConfigs: [[url: "https://github.com/${repo}"]],
         branches: [[name: "refs/heads/${branch}"]],
         extensions: [[$class: 'RelativeTargetDirectory',
                       relativeTargetDir: relative_target_dir]],
         doGenerateSubmoduleConfigurations: false,
         submoduleCfg: []]
    )
}

def notify_irc(Map args) {
    def command = "${env.WORKSPACE}/scripts/irc-notify.sh"
    for (arg in args) {
        command += " --${arg.key} '${arg.value}'"
    }
    sh command
}

def make(cmd, cmd_display) {
    def target = get_target()
    def nick = "mdn${target}push"
    def tag = env.GIT_COMMIT.take(7)
    def repo_upper = get_repo().toUpperCase()
    try {
        /*
         * Run the actual make command within the proper environment.
         */
        sh """
            . regions/portland/${target}.sh
            make ${cmd} ${repo_upper}_IMAGE_TAG=${tag}
        """
        notify_irc([
            irc_nick: nick,
            stage: cmd_display,
            status: 'success'
        ])
    } catch(err) {
        notify_irc([
            irc_nick: nick,
            stage: cmd_display,
            status: 'failure'
        ])
        throw err
    }
}

def migrate_db() {
    /*
     * Migrate the database (only for kuma).
     */
    if (get_repo() == 'kuma') {
        make('k8s-db-migration-job', 'Migrate Database')
    }
}

def rollout() {
    /*
     * Start a rolling update.
     */
    def repo = get_repo()
    make("k8s-${repo}-deployments", 'Start Rollout')
}

def monitor_rollout() {
    /*
     * Monitor the rolling update until it succeeds or fails.
     */
    def repo = get_repo()
    make("k8s-${repo}-rollout-status", 'Check Rollout Status')
}

def announce_push() {
    /*
     * Announce the push.
     */
    def repo = get_repo()
    def target = get_target()
    def tag = env.GIT_COMMIT.take(7)
    notify_irc([
        irc_nick: "mdn${target}push",
        status: "Pushing to ${target}",
        message: "${repo} image ${tag}"
    ])
}

return this;
