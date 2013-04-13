# with mysql, nginx, rvm, ruby installed
set :deploy_to do
  path = "/mnt/site/#{application}"
end

# roles details
#role :web, "ewhine@122.192.64.200"                          # Your HTTP server, Apache/etc
#role :app, "ewhine@122.192.64.200"                          # This may be the same as your `Web` server
#role :db,  "ewhine@122.192.64.200", :primary => true        # This is where Rails migrations will run
# role :db,  "your slave db-server here"
server 'ewhine@minxing365.com', :app, :web, :db, :primary => true
ssh_options[:port] = 22

