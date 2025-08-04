package com.github.phanerozoicc.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContextUtil : ApplicationContextAware {
    companion object {
        private lateinit var applicationContext: ApplicationContext

        fun <T> getBean(clazz: Class<T>): T {
            return applicationContext.getBean(clazz)
        }

        fun getBean(beanName: String): Any? {
            return applicationContext.getBean(beanName)
        }

        fun <T> getBean(beanName: String, clazz: Class<T>): T {
            return applicationContext.getBean(beanName, clazz)
        }
    }

    override fun setApplicationContext(context: ApplicationContext) {
        SpringContextUtil.applicationContext = context
    }
}