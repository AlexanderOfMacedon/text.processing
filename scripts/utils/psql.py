import postgresql
from random import randint

class Postresql:
    def __init__(self, database_info):

        self.db_request = 'pq://{}:{}@{}:{}/{}'.format(database_info['user'], database_info['password'],
                                                       database_info['host'], database_info['port'],
                                                       database_info['database'])

    def createTable(self, table_name, columns):
        query = 'CREATE TABLE ' + table_name + ' ('
        for column in columns:
            query = query + ' ' + column + ' ' + columns[column] + ','
        query = query + 'id BIGSERIAL)'
        # try:
        # print(query)
        with postgresql.open(self.db_request) as db:
            db.execute(query)
        return 'Success'
        # except psycopg2.errors.DuplicateTable:
        #     return 'False'

    def deleteTable(self, table_name):
        query = 'DROP TABLE IF EXISTS ' + table_name
        with postgresql.open(self.db_request) as db:
            db.execute(query)
        return 'Success'

    def insert(self, table_name, insert_data):
        if not insert_data:
            return 'Success'
        new_insert_data = []
        for ins in insert_data:
            new_insert_data.append({key: ins[key] for key in ins if not key == 'id'})
        query = 'INSERT INTO ' + table_name + ' ('
        for column in new_insert_data[0]:
            if not column == 'id':
                query = query + ' ' + column + ','
        query = query[:-1] + ' ) VALUES ('
        count = 1
        for index in range(len(list(new_insert_data[0].keys()))):
            query = query + ' $' + str(count) + ','
            count += 1
        query = query[:-1] + ')'
        with postgresql.open(self.db_request) as db:
            ins = db.prepare(query)
            for insert_row in new_insert_data:
                ins(*tuple(list(insert_row.values()) ))
        return 'Success'

    def select(self, table_name, where_data = None):
        query = 'SELECT * FROM ' + table_name
        if where_data:
            keys = list(where_data.keys())
            query += ' WHERE '
            for index in range(len(keys)-1):
                if isinstance(where_data[keys[index]], str):
                    query += keys[index] + ' = \'' + where_data[keys[index]] + '\' AND '
                else:
                    query += keys[index] + ' = ' + str(where_data[keys[index]]) + ' AND '
            if isinstance(where_data[keys[len(keys)-1]], str):
                query += keys[len(keys)-1] + ' = \'' + where_data[keys[len(keys)-1]] + '\''
            else:
                query += keys[len(keys) - 1] + ' = ' + str(where_data[keys[len(keys) - 1]])
        with postgresql.open(self.db_request) as db:
            response = db.query(query)
        return [dict(row) for row in response]

    def update(self, table_name, insert_data):
        query = 'UPDATE ' + table_name + ' SET '
        updated_keys = list(insert_data.keys())
        updated_keys.remove('id')
        for index in range(len(updated_keys) - 1):
            query = query + updated_keys[index] + ' = $' + str(index + 1) + ', '
        query = query + updated_keys[len(updated_keys) - 1] + ' = $' + str(len(updated_keys)) + ' WHERE id = ' \
                + str(insert_data['id'])
        with postgresql.open(self.db_request) as db:
            ins = db.prepare(query)
            ins(*tuple((insert_data[key] for key in updated_keys)))
        return 'Success'

    def delete(self, table_name, data):
        query = 'DELETE FROM {} WHERE id = {}'.format(table_name, data['id'])
        with postgresql.open(self.db_request) as db:
            db.execute(query)
        return 'Success'

    def columns(self, table_name):
        query = 'SELECT * FROM information_schema.columns WHERE table_name = \'' + table_name + '\''
        with postgresql.open(self.db_request) as db:
            response = db.query(query)
        return [col[3] for col in response]






