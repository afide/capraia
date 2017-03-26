package com.capraia.integration.extension

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * Spock environment configuration annotation extension
 */
class NodeExtension extends AbstractAnnotationDrivenExtension<Node> {

    private static FieldInfo field

    @Override void visitFieldAnnotation(Node annotation, FieldInfo field) {
        this.field = field
    }

    @Override void visitSpec(SpecInfo spec) {
        NodeInterceptor.install(spec, field)
    }
}

/**
 * Environment configuration interceptor
 */
class NodeInterceptor extends AbstractMethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(NodeInterceptor.class)
    private static ConfigObject config
    private final FieldInfo field

    static {
        config = new ConfigSlurper(System.getProperty('node')).parse(com.capraia.integration.env.Node.class)
        logger.info("Environment End Point: $config.node.name\n")
    }

    NodeInterceptor(FieldInfo field) {
        this.field = field
    }

    @Override void interceptSpecExecution(IMethodInvocation invocation) {
        def configField = invocation.spec.fields.find { it.type == ConfigObject }
        if (!configField) {
            logger.error("Fields annotated with $Node must be of type $ConfigObject")
        } else {
            invocation.proceed()
        }
    }

    void injectNode(target) {
        field.writeValue(target, config)
    }

    void cleanupNode(target) {
        field.writeValue(target, null)
    }

    static void install(SpecInfo spec, FieldInfo field) {
        installForInstanceHandlers(spec, field)
        installForSharedHandlers(spec, field)
    }

    private static void installForSharedHandlers(SpecInfo spec, FieldInfo field) {
        if (field) {
            def sharedInterceptor = new NodeInterceptor(field) {
                @Override
                void interceptSetupSpecMethod(IMethodInvocation invocation) {
                    invocation.proceed()
                    injectNode(invocation.sharedInstance)
                }

                @Override
                void interceptCleanupSpecMethod(IMethodInvocation invocation) {
                    cleanupNode(invocation.sharedInstance)
                    invocation.proceed()
                }
            }
            spec.addInterceptor sharedInterceptor
            spec.addSetupSpecInterceptor sharedInterceptor
            spec.addCleanupSpecInterceptor sharedInterceptor
        }
    }

    private static void installForInstanceHandlers(SpecInfo spec, FieldInfo field) {
        if (field) {
            def interceptor = new NodeInterceptor(field) {
                @Override
                void interceptSetupMethod(IMethodInvocation invocation) {
                    invocation.proceed()
                    injectNode(invocation.instance)
                }

                @Override
                void interceptCleanupMethod(IMethodInvocation invocation) {
                    cleanupNode(invocation.instance)
                    invocation.proceed()
                }
            }
            spec.addInterceptor interceptor
            spec.addSetupInterceptor interceptor
            spec.addCleanupInterceptor interceptor
        }
    }
}