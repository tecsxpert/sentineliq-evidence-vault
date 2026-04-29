function Home({ setPage }) {
  return (
    <div className="flex flex-col items-center justify-center h-[80vh] text-center">

      <h1 className="text-4xl font-bold mb-4 text-slate-800">
        Evidence Vault
      </h1>

      <p className="text-gray-600 mb-6 max-w-xl">
        Securely manage and track evidence records with ease.
        Built for efficiency, transparency, and reliability.
      </p>

      <div className="flex gap-4">
        <button
          onClick={() => setPage("login")}
          className="bg-indigo-500 hover:bg-indigo-600 text-white px-5 py-2 rounded"
        >
          Login
        </button>

        <button
          onClick={() => setPage("register")}
          className="bg-gray-300 hover:bg-gray-400 px-5 py-2 rounded"
        >
          Register
        </button>
      </div>

    </div>
  );
}

export default Home;