package com.github.phanerozoicc.statemachine.impl

import com.github.phanerozoicc.statemachine.Action
import com.github.phanerozoicc.statemachine.Condition
import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.Transition
import mu.KLogging

class TransitionImpl<S, E, C>(
    override var source: State<S, E, C>,
    override var target: State<S, E, C>,
    override var event: E,
    override var type: TransitionType = TransitionType.EXTERNAL
): Transition<S, E, C> {

    companion object: KLogging()

    override var condition: Condition<C>? = null
    override var action: Action<S, E, C>? = null

    override fun transit(context: C, checkCondition: Boolean): State<S, E, C>  {
        logger.debug { "do transition: $this" }
        this.verify()
        if (!checkCondition || condition==null || condition!!.isSatisfied(context)) {
            action?.execute(source.id, target.id, event, context)
            return target
        }
        logger.debug("condition is not satisfied, stay at the {} state", source)
        return source;
    }

    override fun verify() {
        if (type == TransitionType.INTERNAL && source!=target) {
            throw IllegalStateException("INTERNAL transition must have same source and target state")
        }
    }

    override fun toString(): String {
        return "${source}-[${event}]->${target}"
    }

    override fun equals(other: Any?): Boolean {
        if (other is Transition<*, *, *>) {
            return this.event!!.equals(other.event)
                    && this.source.equals(other.source)
                    && this.target.equals(other.target)
        }
        return false
    }

}