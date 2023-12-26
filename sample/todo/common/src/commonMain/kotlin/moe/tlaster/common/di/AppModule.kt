package moe.tlaster.common.di

import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.common.viewmodel.NoteDetailViewModel
import moe.tlaster.common.viewmodel.NoteEditViewModel
import moe.tlaster.common.viewmodel.NoteListViewModel
import moe.tlaster.precompose.stateholder.SavedStateHolder
import org.koin.dsl.module

object AppModule {
    val appModule = module {
        single { FakeRepository() }
        factory { (id: Int) ->
            NoteDetailViewModel(
                id = id,
                fakeRepository = get(),
            )
        }
        factory { (id: Int?, savedStateHolder: SavedStateHolder) ->
            NoteEditViewModel(
                id = id,
                savedStateHolder = savedStateHolder,
                fakeRepository = get(),
            )
        }
        factory { NoteListViewModel(fakeRepository = get()) }
    }
}
