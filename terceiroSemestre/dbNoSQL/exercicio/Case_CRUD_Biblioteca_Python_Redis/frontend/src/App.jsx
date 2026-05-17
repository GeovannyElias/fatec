import { useEffect, useMemo, useState } from "react";
import {
  BookOpen,
  CheckCircle2,
  ChevronDown,
  Clock3,
  Heart,
  Library,
  LogIn,
  LogOut,
  Menu,
  Plus,
  RefreshCw,
  Search,
  Shield,
  Trash2,
  User,
  X,
} from "lucide-react";
import { api, createBasicToken } from "./api";

const emptyBook = {
  titulo: "",
  autor: "",
  categoria: "",
  ano: new Date().getFullYear(),
  quantidade: 2,
  imagem: null,
  imagemAtual: "",
};

const BOOK_PLACEHOLDER = "/livros/placeholder.svg";
const VALID_IMAGE_TYPES = ["image/png", "image/jpeg", "image/webp"];

function getBookCover(book) {
  return book?.imagem ? `/livros/${book.imagem}` : BOOK_PLACEHOLDER;
}

function loadSession() {
  const saved = localStorage.getItem("biblioteca_session");
  return saved ? JSON.parse(saved) : null;
}

function groupByAuthor(books) {
  return books.reduce((groups, book) => {
    const author = book.autor || "Autor desconhecido";
    groups[author] = groups[author] || [];
    groups[author].push(book);
    return groups;
  }, {});
}

function getInitials(name) {
  return name
    .trim()
    .split(/\s+/)
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}

function App() {
  const [session, setSession] = useState(loadSession);
  const [page, setPage] = useState("home");
  const [books, setBooks] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [waiting, setWaiting] = useState([]);
  const [loans, setLoans] = useState([]);
  const [users, setUsers] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [authOpen, setAuthOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [pendingAction, setPendingAction] = useState(null);
  const [confirmDialog, setConfirmDialog] = useState(null);
  const [toast, setToast] = useState(null);
  const [loading, setLoading] = useState(false);

  const isAdmin = session?.cargo === "Admin";
  const isUser = session?.cargo === "User";

  const waitingIds = useMemo(() => new Set(waiting.map((book) => Number(book.id))), [waiting]);
  const favoriteIds = useMemo(() => new Set(favorites.map((book) => Number(book.id))), [favorites]);
  const groupedBooks = useMemo(() => groupByAuthor(books), [books]);

  function showToast(type, message) {
    setToast({ type, message });
    setTimeout(() => setToast(null), 1500);
  }

  function saveSession(nextSession) {
    setSession(nextSession);
    localStorage.setItem("biblioteca_session", JSON.stringify(nextSession));
  }

  function logout() {
    localStorage.removeItem("biblioteca_session");
    setSession(null);
    setBooks([]);
    setFavorites([]);
    setWaiting([]);
    setLoans([]);
    setUsers([]);
    setNotifications([]);
    setPage("home");
    showToast("success", "Sessao encerrada");
  }

  async function refreshAll(activeSession = session) {
    setLoading(true);
    try {
      const livros = await api.listarLivros(activeSession);
      setBooks(livros.sort((a, b) => a.autor.localeCompare(b.autor) || a.titulo.localeCompare(b.titulo)));

      if (activeSession?.cargo === "User") {
        const [fav, wait, loan, note] = await Promise.all([
          api.listarFavoritos(activeSession),
          api.listarEspera(activeSession),
          api.listarEmprestimos(activeSession),
          api.listarNotificacoes(activeSession),
        ]);
        setFavorites(fav);
        setWaiting(wait);
        setLoans(loan);
        setNotifications(note.notificacoes || []);
        setUsers([]);
      } else if (activeSession?.cargo === "Admin") {
        const adminUsers = await api.listarUsuarios(activeSession);
        setUsers(adminUsers);
        setFavorites([]);
        setWaiting([]);
        setLoans([]);
        setNotifications([]);
      } else {
        setFavorites([]);
        setWaiting([]);
        setLoans([]);
        setUsers([]);
        setNotifications([]);
      }
    } catch (error) {
      showToast("error", error.message);
    } finally {
      setLoading(false);
    }
  }

  async function runAction(action, successMessage) {
    try {
      await action();
      showToast("success", successMessage);
      await refreshAll();
    } catch (error) {
      showToast("error", error.message);
    }
  }

  function requestConfirmation({ title, message, confirmText = "Confirmar", variant = "default" }) {
    return new Promise((resolve) => {
      setConfirmDialog({
        title,
        message,
        confirmText,
        variant,
        onResolve: resolve,
      });
    });
  }

  function closeConfirmation(result) {
    if (confirmDialog?.onResolve) {
      confirmDialog.onResolve(result);
    }
    setConfirmDialog(null);
  }

  async function confirmAndRun(config, action, successMessage) {
    const confirmed = await requestConfirmation(config);
    if (!confirmed) return;

    await runAction(action, successMessage);
  }

  useEffect(() => {
    refreshAll();
  }, [session]);

  async function runBookAction(type, id, activeSession = session, askConfirm = true) {
    const actions = {
      borrow: {
        confirm: "Confirma o emprestimo deste livro?",
        success: "Livro emprestado com sucesso",
        request: () => api.emprestar(id, activeSession),
      },
      favorite: {
        confirm: "Adicionar este livro aos favoritos?",
        success: "Livro adicionado aos favoritos",
        request: () => api.favoritar(id, activeSession),
      },
      waiting: {
        confirm: "Entrar na lista de espera deste livro?",
        success: "Voce entrou na espera deste livro",
        request: () => api.entrarEspera(id, activeSession),
      },
    };

    const action = actions[type];
    if (!action) return;

    if (!activeSession) {
      requireLogin({ type, id });
      return;
    }

    if (askConfirm) {
      const confirmed = await requestConfirmation({
        title: "Confirmar acao",
        message: action.confirm,
        confirmText: "Continuar",
      });

      if (!confirmed) return;
    }

    try {
      await action.request();
      showToast("success", action.success);
      await refreshAll(activeSession);
    } catch (error) {
      showToast("error", error.message);
    }
  }

  function requireLogin(action = null) {
    setPendingAction(action);
    showToast("error", "Faca login para continuar");
    setAuthOpen(true);
  }

  return (
    <div className="min-h-screen bg-paper text-ink">
      <Header
        page={page}
        setPage={setPage}
        session={session}
        isAdmin={isAdmin}
        isUser={isUser}
        userMenuOpen={userMenuOpen}
        setUserMenuOpen={setUserMenuOpen}
        onLoginClick={() => setAuthOpen(true)}
        onLogout={logout}
      />

      <main className="mx-auto min-h-screen w-full max-w-7xl px-4 pb-16 pt-28 sm:px-6 lg:px-8">
        <Hero session={session} onLoginClick={() => setAuthOpen(true)} />

        <>
          {page === "home" && (
            <HomePage
              groupedBooks={groupedBooks}
              loading={loading}
              canUseActions={!session || isUser}
              favoriteIds={favoriteIds}
              waitingIds={waitingIds}
              onBorrow={(id) => runBookAction("borrow", id)}
              onFavorite={(id) => runBookAction("favorite", id)}
              onWaiting={(id) => runBookAction("waiting", id)}
            />
          )}

            {page === "user" && isUser && (
              <UserPage
                loans={loans}
                books={books}
                favorites={favorites}
                waiting={waiting}
                notifications={notifications}
                onRemoveNotification={(index) =>
                  runAction(
                    () => api.removerNotificacao(index, session),
                    "Notificacao removida"
                  )
                }
                onReturn={(id) =>
                  confirmAndRun(
                    {
                      title: "Devolver livro",
                      message: "Confirma a devolucao deste livro?",
                      confirmText: "Devolver",
                    },
                    () => api.devolver(id, session),
                    "Livro devolvido com sucesso"
                  )
                }
                onRemoveFavorite={(id) =>
                  confirmAndRun(
                    {
                      title: "Remover favorito",
                      message: "Remover este livro dos favoritos?",
                      confirmText: "Remover",
                    },
                    () => api.removerFavorito(id, session),
                    "Livro removido dos favoritos"
                  )
                }
              />
            )}

            {page === "admin" && isAdmin && (
              <AdminPage
                books={books}
                users={users}
                session={session}
                onConfirm={requestConfirmation}
                onDone={async (message) => {
                  showToast("success", message);
                  await refreshAll();
                }}
                onError={(message) => showToast("error", message)}
              />
            )}
        </>
      </main>

      <Footer />

      {authOpen && (
        <AuthModal
          onClose={() => {
            setPendingAction(null);
            setAuthOpen(false);
          }}
          onSuccess={async (nextSession) => {
            saveSession(nextSession);
            setAuthOpen(false);
            setPage(nextSession.cargo === "Admin" ? "admin" : "home");
            showToast("success", `Bem-vindo, ${nextSession.username}`);
            if (pendingAction && nextSession.cargo === "User") {
              await runBookAction(pendingAction.type, pendingAction.id, nextSession, true);
            }
            setPendingAction(null);
          }}
          onError={(message) => showToast("error", message)}
        />
      )}

      {toast && <Toast toast={toast} onClose={() => setToast(null)} />}

      {confirmDialog && (
        <ConfirmModal
          dialog={confirmDialog}
          onCancel={() => closeConfirmation(false)}
          onConfirm={() => closeConfirmation(true)}
        />
      )}
    </div>
  );
}

function Header({
  page,
  setPage,
  session,
  isAdmin,
  isUser,
  userMenuOpen,
  setUserMenuOpen,
  onLoginClick,
  onLogout,
}) {
  const initials = session ? getInitials(session.username) : "";

  return (
    <header className="fixed left-0 top-0 z-30 w-full border-b border-ink/10 bg-paper/95 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <button
          className="flex items-center gap-3 text-left"
          onClick={() => setPage("home")}
          title="Ir para inicio"
        >
          <span className="grid h-10 w-10 place-items-center rounded-lg bg-moss text-white">
            <Library size={22} />
          </span>
          <span>
            <span className="block text-base font-bold">Biblioteca Municipal Online</span>
            <span className="hidden text-xs text-ink/60 sm:block">Python + Redis + React</span>
          </span>
        </button>

        <nav className="hidden items-center gap-2 md:flex">
          <NavButton active={page === "home"} onClick={() => setPage("home")}>
            Livros
          </NavButton>
        </nav>

        <div className="hidden items-center gap-2 md:flex">
          {session ? (
            <div className="relative">
              <button
                className="flex items-center gap-3 rounded-lg bg-white px-2 py-2 text-left shadow-sm transition hover:bg-cloud"
                onClick={() => setUserMenuOpen(!userMenuOpen)}
              >
                <span className="grid h-9 w-9 place-items-center rounded-lg bg-moss text-sm font-bold text-white">
                  {initials}
                </span>
                <span className="hidden pr-2 sm:block">
                  <span className="block text-sm font-bold">{session.username}</span>
                  <span className="block text-xs font-semibold text-ink/55">{session.cargo}</span>
                </span>
              </button>

              {userMenuOpen && (
                <div className="absolute right-0 mt-2 w-56 rounded-lg border border-ink/10 bg-white p-2 shadow-soft">
                  <div className="border-b border-ink/10 px-3 py-2">
                    <p className="text-sm font-bold">{session.username}</p>
                    <p className="text-xs font-semibold text-ink/55">{session.cargo}</p>
                  </div>
                  {isUser && (
                    <MenuButton
                      onClick={() => {
                        setPage("user");
                        setUserMenuOpen(false);
                      }}
                    >
                      <User size={17} />
                      Minha conta
                    </MenuButton>
                  )}
                  {isAdmin && (
                    <MenuButton
                      onClick={() => {
                        setPage("admin");
                        setUserMenuOpen(false);
                      }}
                    >
                      <Shield size={17} />
                      Pagina admin
                    </MenuButton>
                  )}
                  <MenuButton
                    onClick={() => {
                      setUserMenuOpen(false);
                      onLogout();
                    }}
                  >
                    <LogOut size={17} />
                    Sair
                  </MenuButton>
                </div>
              )}
            </div>
          ) : (
            <button
              className="inline-flex items-center gap-2 rounded-lg bg-moss px-4 py-2 text-sm font-bold text-white shadow-sm transition hover:bg-moss/90"
              onClick={onLoginClick}
            >
              <LogIn size={18} />
              Entrar
            </button>
          )}
        </div>

        <div className="relative md:hidden">
          <IconButton
            title="Abrir menu"
            onClick={() => setUserMenuOpen(!userMenuOpen)}
          >
            <Menu size={20} />
          </IconButton>

          {userMenuOpen && (
            <div className="absolute right-0 mt-2 w-72 rounded-lg border border-ink/10 bg-white p-2 shadow-soft">
              {session && (
                <div className="mb-1 flex items-center gap-3 border-b border-ink/10 px-3 py-3">
                  <span className="grid h-10 w-10 place-items-center rounded-lg bg-moss text-sm font-bold text-white">
                    {initials}
                  </span>
                  <div>
                    <p className="text-sm font-bold">{session.username}</p>
                    <p className="text-xs font-semibold text-ink/55">{session.cargo}</p>
                  </div>
                </div>
              )}

              {!session && (
                <MenuButton
                  onClick={() => {
                    setUserMenuOpen(false);
                    onLoginClick();
                  }}
                >
                  <LogIn size={17} />
                  Logar
                </MenuButton>
              )}

              {session && (
                <MenuButton
                  onClick={() => {
                    setPage("home");
                    setUserMenuOpen(false);
                  }}
                >
                  <BookOpen size={17} />
                  Ver todos livros
                </MenuButton>
              )}

              {isUser && (
                <MenuButton
                  onClick={() => {
                    setPage("user");
                    setUserMenuOpen(false);
                  }}
                >
                  <User size={17} />
                  Minha conta
                </MenuButton>
              )}

              {isAdmin && (
                <MenuButton
                  onClick={() => {
                    setPage("admin");
                    setUserMenuOpen(false);
                  }}
                >
                  <Shield size={17} />
                  Painel Adm
                </MenuButton>
              )}

              {session && (
                <MenuButton
                  onClick={() => {
                    setUserMenuOpen(false);
                    onLogout();
                  }}
                >
                  <LogOut size={17} />
                  Sair
                </MenuButton>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

function MenuButton({ children, ...props }) {
  return (
    <button
      className="mt-1 flex w-full items-center gap-2 rounded-lg px-3 py-2 text-left text-sm font-bold text-ink/75 transition hover:bg-paper hover:text-ink"
      {...props}
    >
      {children}
    </button>
  );
}

function NavButton({ active, children, ...props }) {
  return (
    <button
      className={`rounded-lg px-4 py-2 text-sm font-semibold transition ${
        active ? "bg-white text-moss shadow-sm" : "text-ink/70 hover:bg-white/70 hover:text-ink"
      }`}
      {...props}
    >
      {children}
    </button>
  );
}

function IconButton({ children, className = "", ...props }) {
  return (
    <button
      className={`grid h-10 w-10 place-items-center rounded-lg bg-white text-ink shadow-sm transition hover:bg-cloud ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}

function Hero({ session, onLoginClick }) {
  return (
    <section className="mb-8 overflow-hidden rounded-lg bg-ink text-white shadow-soft">
      <div className="grid gap-6 px-6 py-8 md:grid-cols-[1.3fr_0.7fr] md:px-8">
        <div>
          <p className="mb-2 text-sm font-semibold uppercase tracking-[0.18em] text-moss">
            Acervo digital
          </p>
          <h1 className="text-3xl font-bold sm:text-4xl">Biblioteca Municipal Online</h1>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-white/75">
            Consulte livros, controle emprestimos e acompanhe favoritos e listas de espera usando
            Redis como base da aplicacao.
          </p>
        </div>
        <div className="flex items-end justify-start md:justify-end">
          {!session && (
            <button
              className="inline-flex items-center gap-2 rounded-lg bg-moss px-5 py-3 text-sm font-bold text-white"
              onClick={onLoginClick}
            >
              <LogIn size={18} />
              Entrar ou cadastrar
            </button>
          )}
        </div>
      </div>
    </section>
  );
}

function EmptyState({ onLoginClick }) {
  return (
    <section className="grid place-items-center rounded-lg border border-dashed border-ink/20 bg-white p-10 text-center">
      <BookOpen className="mb-4 text-moss" size={42} />
      <h2 className="text-xl font-bold">Entre para visualizar o acervo</h2>
      <p className="mt-2 max-w-md text-sm text-ink/65">
        O backend protege a listagem de livros, entao faca login como admin ou cadastre um usuario
        comum para navegar.
      </p>
      <button
        className="mt-5 rounded-lg bg-moss px-5 py-3 text-sm font-bold text-white"
        onClick={onLoginClick}
      >
        Abrir login
      </button>
    </section>
  );
}

function HomePage({
  groupedBooks,
  loading,
  canUseActions,
  favoriteIds,
  waitingIds,
  onBorrow,
  onFavorite,
  onWaiting,
}) {
  const authors = Object.keys(groupedBooks);
  const allBooks = authors.flatMap((author) => groupedBooks[author]);

  if (loading) {
    return <SectionMessage icon={<RefreshCw />} title="Carregando livros..." />;
  }

  if (authors.length === 0) {
    return <SectionMessage icon={<BookOpen />} title="Nenhum livro cadastrado ainda" />;
  }

  return (
    <section>
      <div className="mb-4 flex items-end justify-between gap-4">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.16em] text-moss">Acervo</p>
          <h2 className="text-2xl font-bold">Livros disponiveis</h2>
        </div>
        <span className="rounded-full bg-white px-3 py-1 text-xs font-bold text-ink/60 shadow-sm">
          {allBooks.length} livro(s)
        </span>
      </div>
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {allBooks.map((book) => (
          <BookCard
            key={book.id}
            book={book}
            canUseActions={canUseActions}
            isFavorite={favoriteIds.has(Number(book.id))}
            isWaiting={waitingIds.has(Number(book.id))}
            onBorrow={onBorrow}
            onFavorite={onFavorite}
            onWaiting={onWaiting}
          />
        ))}
      </div>
    </section>
  );
}

function BookCard({
  book,
  canUseActions,
  isFavorite,
  isWaiting,
  onBorrow,
  onFavorite,
  onWaiting,
}) {
  const semEstoque = Number(book.quantidade) <= 0;

  return (
    <article
      className={`book-card ${
        isWaiting ? "book-waiting opacity-75" : ""
      }`}
    >
      <div className="book-card-cover">
        <img
          className="book-card-image"
          src={getBookCover(book)}
          alt={`Capa do livro ${book.titulo}`}
          onError={(event) => {
            event.currentTarget.src = BOOK_PLACEHOLDER;
          }}
        />
        <span
          className={`book-card-status ${
            semEstoque ? "book-card-status-unavailable" : "book-card-status-available"
          }`}
        >
          {book.status}
        </span>
      </div>

      <div className="book-card-body">
        <div>
          <h3 className="book-card-title">{book.titulo}</h3>
          <p className="book-card-author">{book.autor}</p>
          <p className="book-card-category">{book.categoria}</p>
        </div>

        <div className="book-card-meta">
          <BookInfo label="Ano" value={book.ano} />
          <BookInfo label="Estoque" value={book.quantidade} />
        </div>

        {isWaiting && (
          <div className="book-card-note">
            Na sua lista de espera
          </div>
        )}
      </div>

      {canUseActions && (
        <div className="book-card-actions">
          <BookActionButton
            label="Emprestar"
            title="Pegar emprestado"
            disabled={semEstoque}
            onClick={() => onBorrow(book.id)}
          >
            <BookOpen size={17} />
          </BookActionButton>
          <BookActionButton
            label="Favoritar"
            title={isFavorite ? "Ja esta nos favoritos" : "Adicionar aos favoritos"}
            disabled={isFavorite}
            onClick={() => onFavorite(book.id)}
          >
            <Heart size={17} fill={isFavorite ? "currentColor" : "none"} />
          </BookActionButton>
          <BookActionButton
            label="Espera"
            title={semEstoque ? "Entrar na espera" : "Espera disponivel apenas sem estoque"}
            disabled={!semEstoque || isWaiting}
            onClick={() => onWaiting(book.id)}
          >
            <Clock3 size={17} />
          </BookActionButton>
        </div>
      )}
    </article>
  );
}

function BookInfo({ label, value }) {
  return (
    <div className="book-card-info">
      <p>{label}</p>
      <strong>{value}</strong>
    </div>
  );
}

function BookActionButton({ children, label, disabled, ...props }) {
  return (
    <button
      className="book-card-action"
      disabled={disabled}
      {...props}
    >
      <span className="book-card-action-icon">{children}</span>
      <span>{label}</span>
    </button>
  );
}

function UserPage({
  loans,
  books,
  favorites,
  waiting,
  notifications,
  onRemoveNotification,
  onReturn,
  onRemoveFavorite,
}) {
  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_1fr]">
      <Panel title="Emprestimos ativos" icon={<BookOpen size={20} />}>
        {loans.length === 0 ? (
          <SmallEmpty text="Nenhum emprestimo ativo." />
        ) : (
          loans.map((loan) => (
            <div key={`${loan.username}-${loan.livro_id}`} className="flex items-center justify-between rounded-lg bg-paper p-3">
              <div>
                <p className="font-bold">
                  {books.find((book) => Number(book.id) === Number(loan.livro_id))?.titulo ||
                    `Livro #${loan.livro_id}`}
                </p>
                <p className="text-sm text-ink/60">
                  Devolucao ate {new Date(Number(loan.devolucao_em) * 1000).toLocaleTimeString()}
                </p>
              </div>
              <button
                className="rounded-lg bg-moss px-3 py-2 text-sm font-bold text-white"
                onClick={() => onReturn(loan.livro_id)}
              >
                Devolver
              </button>
            </div>
          ))
        )}
      </Panel>

      <Panel title="Notificacoes" icon={<CheckCircle2 size={20} />}>
        {notifications.length === 0 ? (
          <SmallEmpty text="Nenhuma notificacao por enquanto." />
        ) : (
          notifications.map((notification, index) => (
            <div
              key={`${notification}-${index}`}
              className="flex items-center justify-between gap-3 rounded-lg bg-moss/10 p-3 text-sm font-semibold text-moss"
            >
              <span>{notification}</span>
              <button
                className="grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-white/80 text-moss transition hover:bg-white"
                onClick={() => onRemoveNotification(index)}
                title="Remover notificacao"
              >
                <X size={16} />
              </button>
            </div>
          ))
        )}
      </Panel>

      <Panel title="Favoritos" icon={<Heart size={20} />}>
        {favorites.length === 0 ? (
          <SmallEmpty text="Nenhum favorito salvo." />
        ) : (
          favorites.map((book) => (
            <div key={book.id} className="flex items-center justify-between gap-3 rounded-lg bg-paper p-3">
              <div className="flex min-w-0 items-center gap-3">
                <img
                  className="h-14 w-10 rounded-md bg-cloud object-cover"
                  src={getBookCover(book)}
                  alt={`Capa do livro ${book.titulo}`}
                  onError={(event) => {
                    event.currentTarget.src = BOOK_PLACEHOLDER;
                  }}
                />
                <div className="min-w-0">
                  <p className="font-bold">{book.titulo}</p>
                  <p className="text-sm text-ink/60">{book.autor}</p>
                </div>
              </div>
              <IconButton title="Remover favorito" onClick={() => onRemoveFavorite(book.id)}>
                <X size={17} />
              </IconButton>
            </div>
          ))
        )}
      </Panel>

      <Panel title="Lista de espera" icon={<Clock3 size={20} />}>
        {waiting.length === 0 ? (
          <SmallEmpty text="Nenhum livro em espera." />
        ) : (
          waiting.map((book) => (
            <div key={book.id} className="flex items-center gap-3 rounded-lg bg-paper p-3 book-waiting">
              <img
                className="h-14 w-10 rounded-md bg-cloud object-cover"
                src={getBookCover(book)}
                alt={`Capa do livro ${book.titulo}`}
                onError={(event) => {
                  event.currentTarget.src = BOOK_PLACEHOLDER;
                }}
              />
              <div>
                <p className="font-bold">{book.titulo}</p>
                <p className="text-sm text-ink/60">{book.autor}</p>
              </div>
            </div>
          ))
        )}
      </Panel>
    </div>
  );
}

function AdminPage({ books, users, session, onConfirm, onDone, onError }) {
  const [form, setForm] = useState(emptyBook);
  const [editingId, setEditingId] = useState(null);
  const [bookModalOpen, setBookModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [userSearchTerm, setUserSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [openSection, setOpenSection] = useState("books");

  const filteredBooks = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    if (!term) return books;

    return books.filter((book) => {
      return (
        book.titulo.toLowerCase().includes(term) ||
        book.autor.toLowerCase().includes(term)
      );
    });
  }, [books, searchTerm]);

  const totalPages = Math.max(1, Math.ceil(filteredBooks.length / 5));
  const pageBooks = filteredBooks.slice((currentPage - 1) * 5, currentPage * 5);
  const filteredUsers = useMemo(() => {
    const term = userSearchTerm.trim().toLowerCase();
    if (!term) return users;

    return users.filter((userItem) => {
      return (
        userItem.username.toLowerCase().includes(term) ||
        userItem.cargo.toLowerCase().includes(term)
      );
    });
  }, [users, userSearchTerm]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  function openCreateModal() {
    setEditingId(null);
    setForm(emptyBook);
    setBookModalOpen(true);
  }

  function openEditModal(book) {
    setEditingId(book.id);
    setForm({
      titulo: book.titulo,
      autor: book.autor,
      categoria: book.categoria,
      ano: Number(book.ano),
      quantidade: Number(book.quantidade),
      imagem: null,
      imagemAtual: book.imagem || "",
    });
    setBookModalOpen(true);
  }

  function closeBookModal() {
    setEditingId(null);
    setForm(emptyBook);
    setBookModalOpen(false);
  }

  async function submit(event) {
    event.preventDefault();

    const message = editingId
      ? "Confirma a atualizacao deste livro?"
      : "Confirma o cadastro deste livro?";

    const confirmed = await onConfirm({
      title: editingId ? "Salvar alteracoes" : "Cadastrar livro",
      message,
      confirmText: editingId ? "Salvar" : "Cadastrar",
    });

    if (!confirmed) return;

    try {
      if (editingId) {
        await api.atualizarLivro(editingId, form, session);
        onDone("Livro atualizado com sucesso");
      } else {
        await api.criarLivro(form, session);
        onDone("Livro cadastrado com sucesso");
      }
      closeBookModal();
    } catch (error) {
      onError(error.message);
    }
  }

  async function removeBook(book) {
    const confirmed = await onConfirm({
      title: "Excluir livro",
      message: `Tem certeza que deseja excluir "${book.titulo}"?`,
      confirmText: "Excluir",
      variant: "danger",
    });

    if (!confirmed) return;

    try {
      await api.deletarLivro(book.id, session);
      onDone("Livro excluido com sucesso");
    } catch (error) {
      onError(error.message);
    }
  }

  async function removeUser(userItem) {
    const confirmed = await onConfirm({
      title: "Excluir usuario",
      message: `Tem certeza que deseja excluir o usuario "${userItem.username}"? Livros emprestados serao devolvidos automaticamente.`,
      confirmText: "Excluir",
      variant: "danger",
    });

    if (!confirmed) return;

    try {
      await api.deletarUsuario(userItem.username, session);
      onDone("Usuario excluido com sucesso");
    } catch (error) {
      onError(error.message);
    }
  }

  return (
    <>
      <DropdownPanel
        title="Acervo cadastrado"
        icon={<Library size={20} />}
        count={books.length}
        open={openSection === "books"}
        onToggle={() => setOpenSection(openSection === "books" ? "" : "books")}
      >
        <div className="mb-5 grid gap-3 lg:grid-cols-[1fr_auto]">
          <label className="relative block">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/45" size={18} />
            <input
              className="h-11 w-full rounded-lg border border-ink/10 bg-paper pl-10 pr-3 text-sm outline-none transition focus:border-moss focus:ring-4 focus:ring-moss/10"
              placeholder="Pesquisar por titulo ou autor"
              value={searchTerm}
              onChange={(event) => {
                setSearchTerm(event.target.value);
                setCurrentPage(1);
              }}
            />
          </label>
          <button
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-moss px-4 py-3 text-sm font-bold text-white transition hover:bg-moss/90"
            onClick={openCreateModal}
          >
            <Plus size={18} />
            Adicionar livro
          </button>
        </div>

        <div className="space-y-3">
          {filteredBooks.length === 0 ? (
            <SmallEmpty text="Nenhum livro encontrado." />
          ) : (
            pageBooks.map((book) => (
              <div
                key={book.id}
                className="grid gap-4 rounded-lg bg-paper p-4 md:grid-cols-[auto_1fr_auto] md:items-center"
              >
                <img
                  className="h-20 w-14 rounded-lg bg-cloud object-cover shadow-sm"
                  src={getBookCover(book)}
                  alt={`Capa do livro ${book.titulo}`}
                  onError={(event) => {
                    event.currentTarget.src = BOOK_PLACEHOLDER;
                  }}
                />
                <div>
                  <p className="font-bold">{book.titulo}</p>
                  <p className="mt-1 text-sm text-ink/60">
                    {book.autor} - {book.categoria} - {book.ano} - estoque {book.quantidade}
                  </p>
                  <span className="mt-3 inline-flex rounded-full bg-white px-3 py-1 text-xs font-bold text-moss shadow-sm">
                    {book.status}
                  </span>
                </div>
                <div className="flex gap-2">
                  <button
                    className="rounded-lg bg-white px-3 py-2 text-sm font-bold text-moss shadow-sm transition hover:bg-cloud"
                    onClick={() => openEditModal(book)}
                  >
                    Alterar
                  </button>
                  <IconButton title="Excluir" onClick={() => removeBook(book)}>
                    <Trash2 size={17} />
                  </IconButton>
                </div>
              </div>
            ))
          )}
        </div>

        <div className="mt-5 flex flex-col items-center justify-between gap-3 border-t border-ink/10 pt-4 sm:flex-row">
          <p className="text-sm font-semibold text-ink/55">
            Mostrando {pageBooks.length} de {filteredBooks.length} livro(s)
          </p>
          <div className="flex items-center gap-2">
            <button
              className="rounded-lg bg-paper px-3 py-2 text-sm font-bold text-ink transition hover:bg-cloud disabled:cursor-not-allowed disabled:opacity-45"
              disabled={currentPage === 1}
              onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
            >
              Anterior
            </button>
            <span className="rounded-lg bg-moss px-3 py-2 text-sm font-bold text-white">
              {currentPage} / {totalPages}
            </span>
            <button
              className="rounded-lg bg-paper px-3 py-2 text-sm font-bold text-ink transition hover:bg-cloud disabled:cursor-not-allowed disabled:opacity-45"
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage((page) => Math.min(totalPages, page + 1))}
            >
              Proxima
            </button>
          </div>
        </div>
      </DropdownPanel>

      <div className="mt-5">
        <DropdownPanel
          title="Usuarios cadastrados"
          icon={<User size={20} />}
          count={users.length}
          open={openSection === "users"}
          onToggle={() => setOpenSection(openSection === "users" ? "" : "users")}
        >
          <div className="mb-5">
            <label className="relative block">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/45" size={18} />
              <input
                className="h-11 w-full rounded-lg border border-ink/10 bg-paper pl-10 pr-3 text-sm outline-none transition focus:border-moss focus:ring-4 focus:ring-moss/10"
                placeholder="Pesquisar por username ou cargo"
                value={userSearchTerm}
                onChange={(event) => setUserSearchTerm(event.target.value)}
              />
            </label>
          </div>

          <div className="space-y-3">
            {filteredUsers.length === 0 ? (
              <SmallEmpty text="Nenhum usuario encontrado." />
            ) : (
              filteredUsers.map((userItem) => (
                <div
                  key={userItem.username}
                  className="grid gap-4 rounded-lg bg-paper p-4 md:grid-cols-[1fr_auto] md:items-center"
                >
                  <div className="flex items-center gap-3">
                    <span className="grid h-10 w-10 place-items-center rounded-lg bg-moss text-sm font-bold text-white">
                      {getInitials(userItem.username)}
                    </span>
                    <div>
                      <p className="font-bold">{userItem.username}</p>
                      {Number(userItem.emprestimos_ativos) > 0 && (
                        <p className="text-sm text-ink/60">
                          {userItem.emprestimos_ativos} livro(s) emprestado(s)
                        </p>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="rounded-full bg-white px-3 py-1 text-xs font-bold text-moss shadow-sm">
                      {userItem.cargo}
                    </span>
                    <IconButton
                      title="Excluir usuario"
                      onClick={() => removeUser(userItem)}
                      className="disabled:cursor-not-allowed disabled:opacity-40"
                      disabled={userItem.username === "admin"}
                    >
                      <Trash2 size={17} />
                    </IconButton>
                  </div>
                </div>
              ))
            )}
          </div>
        </DropdownPanel>
      </div>

      {bookModalOpen && (
        <BookFormModal
          editingId={editingId}
          form={form}
          setForm={setForm}
          onClose={closeBookModal}
          onSubmit={submit}
          onError={onError}
        />
      )}
    </>
  );
}

function BookFormModal({ editingId, form, setForm, onClose, onSubmit, onError }) {
  const [previewUrl, setPreviewUrl] = useState("");

  useEffect(() => {
    if (!form.imagem) {
      setPreviewUrl("");
      return;
    }

    const nextPreviewUrl = URL.createObjectURL(form.imagem);
    setPreviewUrl(nextPreviewUrl);

    return () => URL.revokeObjectURL(nextPreviewUrl);
  }, [form.imagem]);

  function handleImageChange(event) {
    const file = event.target.files?.[0] || null;

    if (!file) {
      setForm({ ...form, imagem: null });
      return;
    }

    if (!VALID_IMAGE_TYPES.includes(file.type)) {
      event.target.value = "";
      onError("Envie uma imagem png, jpg, jpeg ou webp");
      return;
    }

    setForm({ ...form, imagem: file });
  }

  const coverPreview = previewUrl || (form.imagemAtual ? `/livros/${form.imagemAtual}` : BOOK_PLACEHOLDER);

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-ink/45 p-4">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-soft">
        <div className="mb-5 flex items-start justify-between gap-4">
          <div className="flex items-center gap-2">
            <span className="text-moss">
              <Shield size={20} />
            </span>
            <h2 className="text-xl font-bold">
              {editingId ? "Editar livro" : "Cadastrar livro"}
            </h2>
          </div>
          <IconButton title="Fechar" onClick={onClose}>
            <X size={18} />
          </IconButton>
        </div>

        <form className="space-y-4" onSubmit={onSubmit}>
          <div className="grid gap-4 sm:grid-cols-[120px_1fr] sm:items-end">
            <img
              className="h-40 w-full rounded-lg bg-cloud object-cover sm:h-36"
              src={coverPreview}
              alt="Previa da capa"
              onError={(event) => {
                event.currentTarget.src = BOOK_PLACEHOLDER;
              }}
            />
            <label className="block">
              <span className="mb-1 block text-sm font-bold text-ink/70">Imagem da capa</span>
              <input
                className="block w-full rounded-lg border border-ink/10 bg-white px-3 py-2 text-sm outline-none transition file:mr-3 file:rounded-lg file:border-0 file:bg-moss file:px-3 file:py-2 file:text-sm file:font-bold file:text-white focus:border-moss focus:ring-4 focus:ring-moss/10"
                type="file"
                accept=".png,.jpg,.jpeg,.webp,image/png,image/jpeg,image/webp"
                onChange={handleImageChange}
              />
              {editingId && (
                <span className="mt-2 block text-xs font-semibold text-ink/50">
                  Envie uma nova imagem apenas se quiser trocar a capa.
                </span>
              )}
            </label>
          </div>
          <TextInput
            label="Titulo"
            value={form.titulo}
            onChange={(value) => setForm({ ...form, titulo: value })}
          />
          <TextInput
            label="Autor"
            value={form.autor}
            onChange={(value) => setForm({ ...form, autor: value })}
          />
          <TextInput
            label="Categoria"
            value={form.categoria}
            onChange={(value) => setForm({ ...form, categoria: value })}
          />
          <div className="grid grid-cols-2 gap-3">
            <TextInput
              label="Ano"
              type="number"
              value={form.ano}
              onChange={(value) => setForm({ ...form, ano: Number(value) })}
            />
            <TextInput
              label="Quantidade"
              type="number"
              value={form.quantidade}
              onChange={(value) => setForm({ ...form, quantidade: Number(value) })}
            />
          </div>
          <div className="flex gap-2">
            <button className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-moss px-4 py-3 text-sm font-bold text-white">
              <Plus size={18} />
              {editingId ? "Salvar" : "Cadastrar"}
            </button>
            {editingId && (
              <button
                type="button"
                className="rounded-lg bg-ink/10 px-4 py-3 text-sm font-bold text-ink"
                onClick={onClose}
              >
                Cancelar
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
}

function TextInput({ label, value, onChange, type = "text" }) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-bold text-ink/70">{label}</span>
      <input
        className="h-11 w-full rounded-lg border border-ink/10 bg-white px-3 outline-none transition focus:border-moss focus:ring-4 focus:ring-moss/10"
        required
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function Panel({ title, icon, children }) {
  return (
    <section className="rounded-lg bg-white p-5 shadow-sm">
      <div className="mb-4 flex items-center gap-2">
        <span className="text-moss">{icon}</span>
        <h2 className="text-xl font-bold">{title}</h2>
      </div>
      <div className="space-y-3">{children}</div>
    </section>
  );
}

function DropdownPanel({ title, icon, count, open, onToggle, children }) {
  return (
    <section className="rounded-lg bg-white shadow-sm">
      <button
        className="flex w-full items-center justify-between gap-4 p-5 text-left"
        onClick={onToggle}
      >
        <span className="flex items-center gap-2">
          <span className="text-moss">{icon}</span>
          <span className="text-xl font-bold">{title}</span>
          <span className="rounded-full bg-paper px-3 py-1 text-xs font-bold text-ink/55">
            {count}
          </span>
        </span>
        <ChevronDown
          className={`text-moss transition ${open ? "rotate-180" : ""}`}
          size={22}
        />
      </button>

      {open && (
        <div className="border-t border-ink/10 px-5 pb-5 pt-4">
          {children}
        </div>
      )}
    </section>
  );
}

function SmallEmpty({ text }) {
  return <p className="rounded-lg bg-paper p-4 text-sm text-ink/60">{text}</p>;
}

function SectionMessage({ icon, title }) {
  return (
    <section className="grid place-items-center rounded-lg bg-white p-10 text-center shadow-sm">
      <span className="mb-3 text-moss">{icon}</span>
      <h2 className="text-xl font-bold">{title}</h2>
    </section>
  );
}

function AuthModal({ onClose, onSuccess, onError }) {
  const [mode, setMode] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);

    try {
      if (mode === "register") {
        await api.cadastrar({ username, password });
      }

      const token = createBasicToken(username, password);
      const loginData = await api.login({ token });
      onSuccess({ username: loginData.username, cargo: loginData.cargo, token });
    } catch (error) {
      onError(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-ink/45 p-4">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-soft">
        <div className="mb-5 flex items-center justify-between">
          <div>
            <p className="text-sm font-bold uppercase tracking-[0.16em] text-moss">
              {mode === "login" ? "Login" : "Cadastro"}
            </p>
            <h2 className="text-2xl font-bold">
              {mode === "login" ? "Entrar na biblioteca" : "Criar conta"}
            </h2>
          </div>
          <IconButton title="Fechar" onClick={onClose}>
            <X size={18} />
          </IconButton>
        </div>

        {mode === "login" && (
          <div className="mb-4 rounded-lg bg-moss/10 p-3 text-sm leading-6 text-ink/70">
            Acesso local: use <strong>admin/admin</strong> ou crie uma conta de usuario
          </div>
        )}

        <form className="space-y-4" onSubmit={submit}>
          <TextInput label="Username" value={username} onChange={setUsername} />
          <TextInput label="Password" type="password" value={password} onChange={setPassword} />
          <button
            className="inline-flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-moss text-sm font-bold text-white disabled:opacity-60"
            disabled={submitting}
          >
            <User size={18} />
            {submitting ? "Aguarde..." : mode === "login" ? "Entrar" : "Cadastrar e entrar"}
          </button>
        </form>

        <button
          className="mt-4 w-full rounded-lg bg-paper px-4 py-3 text-sm font-bold text-ink"
          onClick={() => setMode(mode === "login" ? "register" : "login")}
        >
          {mode === "login" ? "Criar uma conta" : "Ja tenho conta"}
        </button>
      </div>
    </div>
  );
}

function ConfirmModal({ dialog, onCancel, onConfirm }) {
  const isDanger = dialog.variant === "danger";

  return (
    <div className="fixed inset-0 z-[60] grid place-items-center bg-ink/45 p-4">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-soft">
        <div className="mb-5 flex items-start gap-4">
          <span
            className={`grid h-11 w-11 shrink-0 place-items-center rounded-lg ${
              "bg-moss/10 text-moss"
            }`}
          >
            {isDanger ? <Trash2 size={21} /> : <CheckCircle2 size={21} />}
          </span>
          <div>
            <h2 className="text-xl font-bold">{dialog.title}</h2>
            <p className="mt-2 text-sm leading-6 text-ink/65">{dialog.message}</p>
          </div>
        </div>

        <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <button
            className="rounded-lg bg-paper px-4 py-3 text-sm font-bold text-ink transition hover:bg-cloud"
            onClick={onCancel}
          >
            Cancelar
          </button>
          <button
            className={`rounded-lg px-4 py-3 text-sm font-bold text-white transition ${
              "bg-moss hover:bg-moss/90"
            }`}
            onClick={onConfirm}
          >
            {dialog.confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

function Toast({ toast, onClose }) {
  return (
    <div
      className={`fixed right-5 top-24 z-50 flex w-[calc(100%-2rem)] max-w-md items-center justify-between gap-4 rounded-lg px-4 py-3 text-sm font-bold text-white shadow-soft sm:w-auto sm:min-w-80 ${
        "bg-moss"
      }`}
    >
      <span>{toast.message}</span>
      <button
        className="grid h-7 w-7 shrink-0 place-items-center rounded-md bg-white/15 transition hover:bg-white/25"
        onClick={onClose}
        title="Fechar aviso"
      >
        <X size={16} />
      </button>
    </div>
  );
}

function Footer() {
  return (
    <footer className="mt-12 border-t border-ink/10 bg-white">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-center gap-2 px-4 py-8 text-center text-sm text-ink/60">
        <p className="font-bold text-ink">Biblioteca Municipal Online</p>
        <p>Projeto simples com React, Tailwind, FastAPI, Python, Redis e Docker.</p>
      </div>
    </footer>
  );
}

export default App;
