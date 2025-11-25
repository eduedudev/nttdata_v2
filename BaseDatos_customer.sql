--
-- PostgreSQL database dump
--

\restrict xznitwt9fQK7rXiwzfQIyHayYLVc1rpj489GKXtrvgtdUdENcHdWXyDAlq3eqTk

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
-- Name: customer; Type: TABLE; Schema: public; Owner: nttdata
--

CREATE TABLE public.customer (
    customer_id integer NOT NULL,
    name character varying(100) NOT NULL,
    gender character varying(10),
    identification character varying(20) NOT NULL,
    address character varying(200),
    phone character varying(20),
    password character varying(100) NOT NULL,
    status boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.customer OWNER TO nttdata;

--
-- Name: customer_customer_id_seq; Type: SEQUENCE; Schema: public; Owner: nttdata
--

CREATE SEQUENCE public.customer_customer_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.customer_customer_id_seq OWNER TO nttdata;

--
-- Name: customer_customer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: nttdata
--

ALTER SEQUENCE public.customer_customer_id_seq OWNED BY public.customer.customer_id;


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
-- Name: customer customer_id; Type: DEFAULT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.customer ALTER COLUMN customer_id SET DEFAULT nextval('public.customer_customer_id_seq'::regclass);


--
-- Name: customer customer_identification_key; Type: CONSTRAINT; Schema: public; Owner: nttdata
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_identification_key UNIQUE (identification);


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
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: nttdata
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- PostgreSQL database dump complete
--

\unrestrict xznitwt9fQK7rXiwzfQIyHayYLVc1rpj489GKXtrvgtdUdENcHdWXyDAlq3eqTk

