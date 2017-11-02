stage("Announce") {
    utils.announce_push()
}

stage("Prepare Infra") {
    // Checkout the "mozmeao/infra" repo's "master" branch into the
    // "infra" sub-directory of the current working directory.
    utils.checkout_github('mozmeao/infra', 'master', 'infra')
}

stage('Push') {
    dir('infra/apps/mdn/mdn-aws/k8s') {
        // Run the database migrations.
        utils.migrate_db()
        // Start a rolling update of the Kuma-based deployments.
        utils.rollout()
        // Monitor the rollout until it has completed.
        utils.monitor_rollout()
    }
}
