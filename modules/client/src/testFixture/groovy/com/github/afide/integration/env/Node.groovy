package com.github.afide.integration.env
node {
    name = 'default'
    host {
        ip = '127.0.0.1'
        port = 46657
    }
}

environments {
    one {
        node {
            name = 'capraiauno'
        }
    }
    two {
        node {
            name = 'capraiadue'
        }
    }
    three {
        node {
            name = 'capraiatre'
        }
    }
}