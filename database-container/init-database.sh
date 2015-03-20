echo "Database initialization"
gosu postgres postgres --single -E < /tmp/init-database.sql
echo "Database initialization done"