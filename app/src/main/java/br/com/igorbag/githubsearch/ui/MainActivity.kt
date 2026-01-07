package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        // Removido getAllReposByUserName() daqui para ser chamado pelo botão
    }

    fun setupView() {
        // @TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
    }

    private fun setupListeners() {
        // @TODO 2 - colocar a acao de click do botao confirmar
        btnConfirmar.setOnClickListener {
            saveUserLocal()
            getAllReposByUserName()
        }
    }

    private fun saveUserLocal() {
        // @TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_user_key), nomeUsuario.text.toString())
            apply()
        }
    }

    private fun showUserName() {
        // @TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val savedUser = sharedPref.getString(getString(R.string.saved_user_key), "")
        if (savedUser?.isNotEmpty() == true) {
            nomeUsuario.setText(savedUser)
            getAllReposByUserName()
        }
    }

    fun setupRetrofit() {
        /*
           @TODO 5 -  realizar a Configuracao base do retrofit
        */
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    fun getAllReposByUserName() {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso
        val user = nomeUsuario.text.toString()
        if (user.isNotEmpty()) {
            val call = githubApi.getAllRepositoriesByUser(user)
            call.enqueue(object : Callback<List<Repository>> {
                override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            setupAdapter(it)
                        }
                    } else {
                        Toast.makeText(applicationContext, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    Toast.makeText(applicationContext, "Erro na requisição", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun setupAdapter(list: List<Repository>) {
        /*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo
            passando a listagem dos repositorios
         */
        val repositoryAdapter = RepositoryAdapter(list)
        listaRepositories.layoutManager = LinearLayoutManager(this)
        listaRepositories.adapter = repositoryAdapter

        repositoryAdapter.btnShareLister = { repository ->
            shareRepositoryLink(repository.htmlUrl)
        }
        repositoryAdapter.itemLister = { repository ->
            openBrowser(repository.htmlUrl)
        }
    }

    // @Todo 11 - Colocar esse metodo no click do share item do adapter
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // @Todo 12 - Colocar esse metodo no click item do adapter
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )
    }
}