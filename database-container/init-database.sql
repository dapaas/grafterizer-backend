--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.15
-- Dumped by pg_dump version 9.4.0
-- Started on 2015-03-20 13:20:21

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 165 (class 3079 OID 11645)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1887 (class 0 OID 0)
-- Dependencies: 165
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 163 (class 1259 OID 16418)
-- Name: data_page; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE data_page (
    data_page text NOT NULL,
    metadata text,
    transformation text,
    sparql_endpoint text,
    raw_file text
);


ALTER TABLE data_page OWNER TO postgres;

--
-- TOC entry 162 (class 1259 OID 16410)
-- Name: files; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE files (
    fileid text NOT NULL,
    filename text,
    file bytea
);


ALTER TABLE files OWNER TO postgres;

--
-- TOC entry 164 (class 1259 OID 16436)
-- Name: inputs; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE inputs (
    transformation text,
    files text
);


ALTER TABLE inputs OWNER TO postgres;

--
-- TOC entry 161 (class 1259 OID 16402)
-- Name: transformations; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE transformations (
    uri text NOT NULL,
    name text,
    metadata text,
    clojure text
);


ALTER TABLE transformations OWNER TO postgres;

--
-- TOC entry 1878 (class 0 OID 16418)
-- Dependencies: 163
-- Data for Name: data_page; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY data_page (data_page, metadata, transformation, sparql_endpoint, raw_file) FROM stdin;
\.


--
-- TOC entry 1877 (class 0 OID 16410)
-- Dependencies: 162
-- Data for Name: files; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY files (fileid, filename, file) FROM stdin;
\.


--
-- TOC entry 1879 (class 0 OID 16436)
-- Dependencies: 164
-- Data for Name: inputs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY inputs (transformation, files) FROM stdin;
\.


--
-- TOC entry 1876 (class 0 OID 16402)
-- Dependencies: 161
-- Data for Name: transformations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY transformations (uri, name, metadata, clojure) FROM stdin;
\.


--
-- TOC entry 1770 (class 2606 OID 16425)
-- Name: data_page_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY data_page
    ADD CONSTRAINT data_page_pkey PRIMARY KEY (data_page);


--
-- TOC entry 1768 (class 2606 OID 16417)
-- Name: files_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY files
    ADD CONSTRAINT files_pkey PRIMARY KEY (fileid);


--
-- TOC entry 1766 (class 2606 OID 16409)
-- Name: transformations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY transformations
    ADD CONSTRAINT transformations_pkey PRIMARY KEY (uri);


--
-- TOC entry 1772 (class 2606 OID 16431)
-- Name: data_page_raw_file_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY data_page
    ADD CONSTRAINT data_page_raw_file_fkey FOREIGN KEY (raw_file) REFERENCES files(fileid);


--
-- TOC entry 1771 (class 2606 OID 16426)
-- Name: data_page_transformation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY data_page
    ADD CONSTRAINT data_page_transformation_fkey FOREIGN KEY (transformation) REFERENCES transformations(uri);


--
-- TOC entry 1774 (class 2606 OID 16447)
-- Name: inputs_files_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY inputs
    ADD CONSTRAINT inputs_files_fkey FOREIGN KEY (files) REFERENCES files(fileid);


--
-- TOC entry 1773 (class 2606 OID 16442)
-- Name: inputs_transformation_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY inputs
    ADD CONSTRAINT inputs_transformation_fkey FOREIGN KEY (transformation) REFERENCES transformations(uri);


--
-- TOC entry 1886 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2015-03-20 13:20:28

--
-- PostgreSQL database dump complete
--

