To create the toc database and to insert data.

mysql -u root -p < drop_create_database.sql
mysql -u tocuser -p toc < create_tables.sql
mysql -u tocuser -p toc < insert_history.sql
