package session;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Proof-of-concept implementation of session types (including delegation and
 * recursion) in Java. Session channels are assigned on slots _0, _1, _2, etc.
 * This is WIP; no accept/connect. A type Proc<Cons<C0,Cons<C1,..>>> means that
 * process uses session channel at _0 as C0, _1 as C1, ...
 * 
 * @author keigoi <keigoi@gifu-u.ac.jp>
 */
public class SessionJava {

	public static void main(String[] args) {
		Proc<Cons<Cap<Nil, Send<Integer, Recv<Integer, End>>>, Nil>> x = send(100)._0(recv()._0((Integer i) -> end()));

		Proc<Cons<Cap<Nil, Select<Send<Integer, Recv<Integer, End>>, Send<String, End>>>, Nil>> x2 = left(
				new Protocol<Send<String, End>>())._0(send(100)._0(recv()._0((Integer i) -> end())));

		Proc<Cons<Cap<Nil, Offer<Send<Integer, Recv<Integer, End>>, End>>, Nil>> x3 = offer()
				._0(() -> send(100)._0(recv()._0((Integer i) -> end())), () -> end());

		Proc<Cons<Cap<Nil, DelegSend<Cap<Nil, End>, Send<Integer, Recv<Integer, End>>>>, Cons<Cap<Nil, End>, Nil>>> y = deleg_send()
				._0(deleg_send_target(new Protocol<Cap<Nil, End>>())
						._1(send(100)._0(recv()._0((Integer i) -> end(end())))));

		Proc<Cons<End, Cons<Cap<Nil, DelegRecv<Cap<Nil, DelegSend<Cap<Nil, End>, Send<Integer, Recv<Integer, End>>>>, End>>, Nil>>> z = deleg_recv()
				._1(deleg_recv_target()._0(deleg_send()._0(deleg_send_target(new Protocol<Cap<Nil, End>>())
						._1(send(100)._0(recv()._0((Integer i) -> end(end())))))));

		Proc<Cons<End, Cons<Cap<Nil, DelegRecv<Cap<Nil, DelegSend<Cap<Nil, Send<String, End>>, Send<Integer, Recv<Integer, End>>>>, Send<String, End>>>, Nil>>> w = deleg_recv()
				._1(deleg_recv_target()
						._0(deleg_send()._0(deleg_send_target(new Protocol<Cap<Nil, Send<String, End>>>())
								._1(send(100)._0(recv()._0((Integer i) -> end(end())))))));

		Proc<Cons<Cap<Nil, Rec<Send<Integer, Var<Zero>>>>, Nil>> a = rec_enter()._0(f(0));

	}

	private static Proc<Cons<Cap<Cons<Send<Integer, Var<Zero>>, Nil>, Send<Integer, Var<Zero>>>, Nil>> f(Integer i) {
		return send(i)._0(rec_zero()._0(() -> f(i + 1)));

	}

	protected static Proc<Cons<Cap<Cons<Select<End, Send<Integer, Var<Zero>>>, Nil>, Select<End, Send<Integer, Var<Zero>>>>, Nil>> g(
			Integer i) {

		return i == 0 ? left(new Protocol<Send<Integer, Var<Zero>>>())._0(
				end(new Protocol<>()/* we need this for non-recursive branch */))
				: right(new Protocol<End>())._0(send(i)._0(rec_zero()._0(() -> g(i - 1))));
	}

	protected static Proc<Cons<Cap<Cons<Offer<End, Recv<Integer, Var<Zero>>>, Nil>, Offer<End, Recv<Integer, Var<Zero>>>>, Nil>> h() {

		return offer()._0(() -> end(
				new Protocol<Cons<Offer<End, Recv<Integer, Var<Zero>>>, Nil>>()/* we need this for non-recursive branch */),
				() -> recv()._0((Integer i) -> rec_zero()._0(() -> h())));
	}

	/**
	 * Strongly-typed heterogeneous list constructor
	 * 
	 * @param <S>
	 *            Head.
	 * @param <SS>
	 *            Tail.
	 */
	public static final class Cons<S, SS> {
	}

	/**
	 * Strongly-typed heterogeneous list constructor (empty list).
	 */
	public static final class Nil {
	}

	/**
	 * The class for session-typed processes.
	 * 
	 * @param <SS>
	 *            Capability types for each slot (heterogeneous list)
	 */
	public static class Proc<SS> {
	}

	public static class DelegatingSendProc<S0, SS> {
	}

	public static class DelegatingRecvProc<S0, SS> {
	}

	/**
	 * A session channel of capability type (Pucella & Tov, 2008) assigned into
	 * a slot
	 * 
	 * @param <E>
	 *            List of Unrolled session types
	 * @param <S>
	 *            Session type
	 */
	public static final class Cap<E, S> {
	}

	/**
	 * Finished session
	 */
	public static final class End {
	}

	/**
	 * Input of a value.
	 * 
	 * @param <V>
	 *            Type of received value
	 * @param <S>
	 *            Rest of the session
	 */
	public static final class Recv<V, S> {
	}

	/**
	 * Output of a value.
	 * 
	 * @param <V>
	 *            Type of message sent
	 * @param <S>
	 *            Rest of the session
	 */
	public static final class Send<V, S> {
	}

	/**
	 * Offer a binary choice.
	 * 
	 * @param <S1>
	 *            Left continuation
	 * @param <S2>
	 *            Right continuation
	 */
	public static final class Offer<S1, S2> {
	}

	/**
	 * Select a binary choice.
	 * 
	 * @param <S1>
	 *            Left continuation
	 * @param <S2>
	 *            Right continuation
	 */
	public static final class Select<S1, S2> {
	}

	/**
	 * Delegation (output).
	 * 
	 * @param <C0>
	 *            Capability type of the delegated session
	 * @param <S>
	 *            Rest of the session
	 */
	public static final class DelegSend<C0, S> {
	}

	/**
	 * Delegation (input).
	 * 
	 * @param <C0>
	 *            Capability type of the delegated session
	 * @param <S>
	 *            Rest of the session
	 */
	public static final class DelegRecv<C0, S> {
	}

	/**
	 * Recursive session
	 * 
	 * @param <S>
	 *            Body of the session
	 */
	public static final class Rec<S> {
	}

	/**
	 * Occurrence of recursion variable
	 * 
	 * @param <N>
	 *            Type-level natural number specifying de-Bruijn level of the
	 *            nesting Rec's
	 */
	public static final class Var<N> {
	}

	/**
	 * Type-level natural number N+1
	 * 
	 * @param <N>
	 *            Number
	 */
	public static final class Succ<N> {
	}

	/**
	 * Type-level natural number 0 for recursion variable
	 */
	public static final class Zero {
	}

	/**
	 * Placeholder for types of delegated session
	 * 
	 * @param <C>
	 *            Capability type
	 */
	public static final class Protocol<C> {
	}

	public static class RECV_PROXY {
		public <V, E, S, SS> Proc<Cons<Cap<E, Recv<V, S>>, SS>> _0(Function<V, Proc<Cons<Cap<E, S>, SS>>> pr) {
			return null;
		}
	}

	public static class SEND_PROXY<V> {
		public <E, S, SS> Proc<Cons<Cap<E, Send<V, S>>, SS>> _0(Proc<Cons<Cap<E, S>, SS>> pr) {
			return null;
		}
	}

	public static class OFFER_PROXY {
		public <E, S1, S2, SS> Proc<Cons<Cap<E, Offer<S1, S2>>, SS>> _0(Supplier<Proc<Cons<Cap<E, S1>, SS>>> pr1,
				Supplier<Proc<Cons<Cap<E, S2>, SS>>> pr2) {
			return null;
		}
	}

	public static class SELECT_LEFT_PROXY<S2> {
		public <E, S1, SS> Proc<Cons<Cap<E, Select<S1, S2>>, SS>> _0(Proc<Cons<Cap<E, S1>, SS>> pr) {
			return null;
		}
	}

	public static class SELECT_RIGHT_PROXY<S1> {
		public <E, S2, SS> Proc<Cons<Cap<E, Select<S1, S2>>, SS>> _0(Proc<Cons<Cap<E, S2>, SS>> pr) {
			return null;
		}
	}

	public static class DELEG_SEND_PROXY {
		public <C0, E, S, SS> Proc<Cons<Cap<E, DelegSend<C0, S>>, SS>> _0(
				DelegatingSendProc<C0, Cons<Cap<E, S>, SS>> pr) {
			return null;
		}
	}

	public static class DELEG_SEND_TARGET_PROXY<C0> {
		public <SS> DelegatingSendProc<C0, Cons<C0, SS>> _0(Proc<Cons<Cap<Nil, End>, SS>> pr) {
			return null;
		}

		public <S, SS> DelegatingSendProc<C0, Cons<S, Cons<C0, SS>>> _1(Proc<Cons<S, Cons<Cap<Nil, End>, SS>>> pr) {
			return null;
		}
	}

	public static class DELEG_RECV_PROXY {
		public <C0, E, S, SS> Proc<Cons<Cap<E, DelegRecv<C0, S>>, SS>> _0(
				DelegatingRecvProc<C0, Cons<Cap<E, S>, SS>> pr) {
			return null;
		}

		public <S1, C0, E, S, SS> Proc<Cons<S1, Cons<Cap<E, DelegRecv<C0, S>>, SS>>> _1(
				DelegatingRecvProc<C0, Cons<S1, Cons<Cap<E, S>, SS>>> pr) {
			return null;
		}
	}

	public static class DELEG_RECV_TARGET_PROXY {
		public <S0, SS> DelegatingRecvProc<S0, Cons<End, SS>> _0(Proc<Cons<S0, SS>> pr) {
			return null;
		}

		public <S0, S, SS> DelegatingRecvProc<S0, Cons<S, Cons<End, SS>>> _1(Proc<Cons<S, Cons<S0, SS>>> pr) {
			return null;
		}
	}

	public static class REC_PROXY {
		public <E, S, SS> Proc<Cons<Cap<E, Rec<S>>, SS>> _0(Proc<Cons<Cap<Cons<S, E>, S>, SS>> pr) {
			return null;
		}
	}

	public static class ZERO_PROXY {
		public <E, S, SS> Proc<Cons<Cap<Cons<S, E>, Var<Zero>>, SS>> _0(
				Supplier<Proc<Cons<Cap<Cons<S, E>, S>, SS>>> pr) {
			return null;
		}
	}

	public static class SUCC_PROXY {
		public <N, E, S, SS> Proc<Cons<Cap<Cons<S, E>, Var<Succ<N>>>, SS>> _0(Proc<Cons<Cap<E, Var<N>>, SS>> pr) {
			return null;
		}
	}

	public static Proc<Cons<Cap<Nil, End>, Nil>> end() {
		return null;
	}

	public static <EE> Proc<Cons<Cap<EE, End>, Nil>> end(Protocol<EE> e) {
		return null;
	}

	public static <SS> Proc<Cons<Cap<Nil, End>, SS>> end(Proc<SS> pr) {
		return null;
	}

	public static <EE, SS> Proc<Cons<Cap<EE, End>, SS>> end(Protocol<EE> e, Proc<SS> pr) {
		return null;
	}

	public static <V> SEND_PROXY<V> send(V v) {
		return null;
	}

	public static <V> RECV_PROXY recv() {
		return null;
	}

	public static OFFER_PROXY offer() {
		return null;
	}

	public static <S2> SELECT_LEFT_PROXY<S2> left(Protocol<S2> dummy_right) {
		return null;
	}

	public static <S1> SELECT_RIGHT_PROXY<S1> right(Protocol<S1> dummy_left) {
		return null;
	}

	public static DELEG_SEND_PROXY deleg_send() {
		return null;
	}

	public static <S0> DELEG_SEND_TARGET_PROXY<S0> deleg_send_target(Protocol<S0> dummy) {
		return null;
	}

	public static DELEG_RECV_PROXY deleg_recv() {
		return null;
	}

	public static DELEG_RECV_TARGET_PROXY deleg_recv_target() {
		return null;
	}

	public static REC_PROXY rec_enter() {
		return null;
	}

	public static ZERO_PROXY rec_zero() {
		return null;
	}

	public static SUCC_PROXY rec_succ() {
		return null;
	}

}
