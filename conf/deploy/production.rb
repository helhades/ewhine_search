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

namespace :deploy do

  desc "copy environments file"
  task :copy_env, :roles => :app do
    run "cd #{current_path}/config/environments/production && cp * .."
  end

  desc "start server"
  task :start, :roles => :app do
    run "cd #{current_path} && bundle exec thin start -C config/thin/thin.ewhine.production.yml"
    #run "cd #{current_path} && bundle exec thin start -C config/thin/thin.faye.yml"
    # for some reason, the ssl need you to input the pem code to ssl start, this will hang up the capistrano.
    # run "#{sudo} /etc/init.d/nginx start"
  end
  
  desc "stop server"
  task :stop, :roles => :app do
    run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.ewhine.production.yml"
    #run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.faye.yml"
    #run "#{sudo} /etc/init.d/nginx stop"
    #run "cd #{current_path} && /etc/init.d/thin stop"
  end

desc "restart server"
  task :restart, :roles => :app do
    run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.ewhine.production.yml"
    #run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.faye.yml"
    run "cd #{current_path} && bundle exec thin start -C config/thin/thin.ewhine.production.yml"
    #run "cd #{current_path} && bundle exec thin start -C config/thin/thin.faye.yml"
    #
  end
end

  task :rake_migrate do
    n = Time.now
    backup_timestamp = n.strftime("%Y%m%d%H%M")
    run "cd /mnt/backup && mysqldump -uroot -pesns esns_production | gzip > backup#{backup_timestamp}.sql.gz"
    run "cd #{current_path} && bin/rake db:migrate RAILS_ENV=production"
  end


  task :remote_backup do
    n = Time.now
    backup_timestamp = n.strftime("%Y%m%d%H%M")
    run "cd /mnt/backup && mysqldump -uroot -pesns esns_production | gzip > backup#{backup_timestamp}.sql.gz"
    run_locally "scp -P 22 ewhine@www.minxing365.com:/mnt/backup/backup#{backup_timestamp}.sql.gz tmp/dbbackup.sql.gz"

  end

