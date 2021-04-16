package no.nav.helse.hops.koin

import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.ScopeDSL
import org.koin.dsl.onClose
import java.io.Closeable

/** Helper function to register Closeable as singleton and tie its lifetime to the Module. **/
inline fun <reified T : Closeable> org.koin.core.module.Module.singleClosable(
    qualifier: Qualifier? = null,
    createdAtStart: Boolean = false,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = single(qualifier, createdAtStart, override, definition).onClose { it?.close() }

/** Helper function to register Closeable as scoped and tie its lifetime to the Scope. **/
inline fun <reified T : Closeable> ScopeDSL.scopedClosable(
    qualifier: Qualifier? = null,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = scoped(qualifier, override, definition).onClose { it?.close() }
