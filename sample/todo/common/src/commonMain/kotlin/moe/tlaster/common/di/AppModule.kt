package moe.tlaster.common.di

import moe.tlaster.common.repository.FakeRepository
import moe.tlaster.common.viewmodel.NoteDetailViewModel
import moe.tlaster.common.viewmodel.NoteEditViewModel
import moe.tlaster.common.viewmodel.NoteListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val appModule = module {
        single { FakeRepository() }
        viewModel { (id: Int) ->
            NoteDetailViewModel(
                id = id,
                fakeRepository = get(),
            )
        }

        viewModel { (id: Int) ->
            NoteEditViewModel(
                id = id,
                fakeRepository = get(),
            )
        }
        viewModel { NoteListViewModel(fakeRepository = get()) }
    }
}
