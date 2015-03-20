CREATE DATABASE dapaas
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       CONNECTION LIMIT = -1;

CREATE TABLE files (
    fileid text NOT NULL,
    filename text,
    file bytea
);

CREATE TABLE inputs (
    transformation text,
    files text
);

CREATE TABLE transformations (
    uri text NOT NULL,
    name text,
    metadata text,
    clojure text
);

ALTER TABLE ONLY data_page
    ADD CONSTRAINT data_page_pkey PRIMARY KEY (data_page);

ALTER TABLE ONLY files
    ADD CONSTRAINT files_pkey PRIMARY KEY (fileid);

ALTER TABLE ONLY transformations
    ADD CONSTRAINT transformations_pkey PRIMARY KEY (uri);

ALTER TABLE ONLY data_page
    ADD CONSTRAINT data_page_raw_file_fkey FOREIGN KEY (raw_file) REFERENCES files(fileid);

ALTER TABLE ONLY data_page
    ADD CONSTRAINT data_page_transformation_fkey FOREIGN KEY (transformation) REFERENCES transformations(uri);

ALTER TABLE ONLY inputs
    ADD CONSTRAINT inputs_files_fkey FOREIGN KEY (files) REFERENCES files(fileid);

ALTER TABLE ONLY inputs
    ADD CONSTRAINT inputs_transformation_fkey FOREIGN KEY (transformation) REFERENCES transformations(uri);
