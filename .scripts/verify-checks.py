import requests, sys, getopt, json

argv = sys.argv[1:] # Ignore the program name in argv[0]
commit_sha = None # commit sha of pull request
suites = set() # name of suites to validate
endpoint = 'https://api.github.com/repos/navikt/helseopplysninger/commits/{0}/check-suites'

try: opts, args = getopt.getopt(argv, longopts=["commit_sha", "check_slug"])
except getopt.error as err: print (str(err))

for opt, arg in opts:
    if opt in ['-c', '--commit_sha']: commit_sha = arg
    elif opt in ['-s', '--suite_name']: suites.add(arg)
    else: print("invalid option ", opt)

url = endpoint.format(commit_sha)
response = requests.get(url)
data = response.json()
print(data)
check_suites = data['check_suites']

relevant_checks = list(filter(lambda check: check['app']['slug'] in suites, check_suites))
completed_checks = list(filter(lambda check: check['status'] == 'completed', relevant_checks))
success_checks = list(filter(lambda check: check['conclusion'] == 'success', completed_checks))

if (len(success_checks) == 2):
    print("All checks have passed")
    exit(0)
else:
    print("Completed checks:", success_checks['app']['slug)
    exit(1)
