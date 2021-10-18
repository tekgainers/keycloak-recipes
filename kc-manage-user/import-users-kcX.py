import csv
import requests
import json

###############################################################################################
# Sample Script to add user to Keycloak.X via CSV file
#
###############################################################################################

# getting token to call keycloak.X add user api
accessTokenUrl = "http://localhost:8080/realms/master/protocol/openid-connect/token"


#update the admin credential for your keycloak instance
username='admin'
password='password'
payload='client_id=admin-cli&username='+username+'&password='+password+'&grant_type=password'

headers = {
  'Content-Type': 'application/x-www-form-urlencoded'
}

print('Retrieving Access token from Keycloak')

response = requests.request("POST", accessTokenUrl, headers=headers, data=payload)

access_token=json.loads(response.text)['access_token']

print('Here is the access token'+access_token)

addUserUrl = "http://localhost:8080/admin/realms/master/users"

# read csv file containing users to add
with open('users.csv', newline='') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
    # adding user
        payload="{\r\n    \"username\":\""+row['firstname']+"\",\r\n    \"firstName\":\""+row['firstname']+"\",\r\n    \"lastName\":\""+row['lastname']+"\",\r\n    \"enabled\":true,\r\n    \"emailVerified\":true,\r\n    \"email\":\""+row['email']+"\"\r\n}"
        headers = {
            'Authorization': 'Bearer '+access_token+'',
            'Content-Type': 'application/json'
                   }

        response = requests.request("POST", addUserUrl, headers=headers, data=payload)

        if (response.status_code==201):
            print(row['firstname'] + ' user added successfully')
        elif (response.status_code==409):
            print(row['firstname'] + ' already exist in the Keycloak')