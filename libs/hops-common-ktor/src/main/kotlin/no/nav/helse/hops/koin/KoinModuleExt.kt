package no.nav.helse.hops.koin

import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.ScopeDSL
import org.koin.dsl.onClose
import java.io.Closeable

/** Helper function to register Closeable as singleton and tie its lifetime to the Module. **/
inline fun <reified T : Closeable> Module.singleClosable(
    qualifier: Qualifier? = null,
    createdAtStart: Boolean = false,
    noinline definition: Definition<T>
) = single(qualifier, createdAtStart, definition).onClose { it?.close() }

/** Helper function to register Closeable as scoped and tie its lifetime to the Scope. **/
inline fun <reified T : Closeable> ScopeDSL.scopedClosable(
    qualifier: Qualifier? = null,
    noinline definition: Definition<T>
) = scoped(qualifier, definition).onClose { it?.close() }
