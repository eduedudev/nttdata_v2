--
-- PostgreSQL database dump
--

\restrict 2GRfX0ifKziDbWYtUroCd84ocMDRLmnF1ftdngGjbbLGHI82ifZgM741B32UIqf

-- Dumped from database version 16.11
-- Dumped by pg_dump version 16.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: accounts; Type: TABLE; Schema: public; Owner: nttdata
--

CREATE TABLE public.accounts (
    account_id bigint NOT NULL,
    account_number character varying(50) NOT NULL,
    account_type character varying(20) NOT NULL,
    initial_balance numeric(19,4) DEFAULT 0 NOT NULL,
    current_balance numeric(19,4) DEFAULT 0 NOT NULL,
    status boolean DEFAULT true NOT NULL,
    customer_id bigint NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT accounts_account_type_check CHECK (((account_type)::text = ANY ((ARRAY['SAVINGS'::character varying, 'CHECKING'::character varying])::text[])))
);


ALTER TABLE public.accounts OWNER TO nttdata;

--
-- Name: accounts_account_id_seq; Type: SEQUENCE; Schema: public; Owner: nttdata
--

CREATE SEQUENCE public.accounts_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.accounts_account_id_seq OWNER TO nttdata;

--
-- Name: accounts_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nttdata
--

ALTER SEQUENCE public.accounts_account_id_seq OWNED BY public.accounts.account_id;


--
-- Name: customer; Type: TABLE; Schema: public; Owner: nttdata
--

CREATE TABLE public.customer (
    customer_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    identification character varying(50) NOT NULL,
    address character varying(500),
    phone character varying(50),
    status boolean DEFAULT true
);


ALTER TABLE public.customer OWNER TO nttdata;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: nttdata
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO nttdata;

--
-- Name: movements; Type: TABLE; Schema: public; Owner: nttdata
--

CREATE TABLE public.movements (
    movement_id bigint NOT NULL,
    date timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    movement_type character varying(20) NOT NULL,
    amount numeric(19,4) NOT NULL,
    balance numeric(19,4) NOT NULL,
    description character varying(500),
    account_id bigint NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT movements_movement_type_check CHECK (((movement_type)::text = ANY ((ARRAY['CREDIT'::character varying, 'DEBIT'::character varying])::text[])))
);


ALTER TABLE public.movements OWNER TO nttdata;

--
-- Name: movements_movement_id_seq; Type: SEQUENCE; Schema: public; Owner: nttdata
--

CREATE SEQUENCE public.movements_movement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.movements_movement_id_seq OWNER TO nttdata;

--
-- Name: movements_movement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nttdata
--

ALTER SEQUENCE public.movements_movement_id_seq OWNED BY public.movements.movement_id;


--
-- Name: accounts account_id; Type: DEFAULT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.accounts ALTER COLUMN account_id SET DEFAULT nextval('public.accounts_account_id_seq'::regclass);


--
-- Name: movements movement_id; Type: DEFAULT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.movements ALTER COLUMN movement_id SET DEFAULT nextval('public.movements_movement_id_seq'::regclass);


--
-- Name: accounts accounts_account_number_key; Type: CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_account_number_key UNIQUE (account_number);


--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (account_id);


--
-- Name: customer customer_pkey; Type: CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (customer_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: movements movements_pkey; Type: CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.movements
    ADD CONSTRAINT movements_pkey PRIMARY KEY (movement_id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_accounts_account_number; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX idx_accounts_account_number ON public.accounts USING btree (account_number);


--
-- Name: idx_accounts_customer_id; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX idx_accounts_customer_id ON public.accounts USING btree (customer_id);


--
-- Name: idx_movements_account_date; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX idx_movements_account_date ON public.movements USING btree (account_id, date);


--
-- Name: idx_movements_account_id; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX idx_movements_account_id ON public.movements USING btree (account_id);


--
-- Name: idx_movements_date; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX idx_movements_date ON public.movements USING btree (date);


--
-- Name: accounts accounts_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customer(customer_id);


--
-- Name: movements movements_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.movements
    ADD CONSTRAINT movements_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- PostgreSQL database dump complete
--

\unrestrict 2GRfX0ifKziDbWYtUroCd84ocMDRLmnF1ftdngGjbbLGHI82ifZgM741B32UIqf

