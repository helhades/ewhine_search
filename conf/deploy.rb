# setup rvm
#  Using RVM rubies with Capistrano http://beginrescueend.com/integration/capistrano/
#$:.unshift(File.expand_path('./lib', ENV['rvm_path'])) # Add RVM's lib directory to the load path.
require 'rvm/capistrano'        
set :rvm_ruby_string, 'ruby-1.9.3-p374@ewhine'        # Or whatever env you want it to run in.
set :rvm_type, :user            # system
#ssh_options[:verbose] = :debug
set :keep_releases, 1

# setup bundler
require 'capistrano_colors'
require "bundler/capistrano"

set :application, "ewhine_search"
#set :stages, %w(production staging ec2)
#set :default_stage, "staging"
#require 'capistrano/ext/multistage'

# repo details
set :scm, :git
set :repository,  "git@github.com:jimrok/ewhine_search.git"
#set :repository,  "https://jimrok@bitbucket.org/imbeastmaster/ewhine.git"
# set :git_enable_submodules, 1 # if you have vendored rails
set :branch, 'master'
set :git_shallow_clone, 1
set :scm_verbose, true

ssh_options[:forward_agent] = true
ssh_options[:port] = 2222

# deployment details
set :use_sudo, false
set :deploy_via, :remote_cache

#set :deploy_via, :copy
set :copy_cache, true

set :group_writable, false

set :default_environment, {
  'LANG' => 'zh_CN.UTF-8'
 }


default_run_options[:shell] = false 
default_run_options[:pty] = true

set :deploy_to, "/home/ewhine/deploy/#{application}"
server 'ewhine@www.weixinwork.com', :app, :web, :db, :primary => true

#after "deploy:finalize_update","deploy:compile"
namespace :bundle do
  task :install do
    run "echo install"
  end
end

namespace :deploy do

  task :finalize_update do
    #run "cd #{latest_release} ; ant jar"
  end

end

after "deploy:setup","deploy:file_store"

namespace :deploy do

  task :file_store do
    run "mkdir -p #{deploy_to}/indexs"
  end

end

#skipping asset pre-compilation if no change happen.
#Only support git.


after "deploy:create_symlink", "deploy:link_file_store"
before "all","deploy:stop","deploy:update","deploy:start"
#before "deploy:assets:precompile","deploy:assets:clean_assets"
#after "deploy:update", "deploy:cleanup"

namespace :deploy do
  task :link_file_store do
    run "cd #{current_path} && ln -s #{deploy_to}/indexs indexs"
  end

end


namespace :deploy do
      namespace :assets do
        task :precompile, :roles => :web, :except => { :no_release => true } do
          #run "cd #{latest_release} ; nohup #{rake} RAILS_ENV=#{rails_env} #{asset_env} assets:precompile >> log/asset.log 2>&1 &", :pty => false
        end
       task :symlink, :roles => :web, :except => { :no_release => true } do
          #run ""
          run "cd #{latest_release}; mkdir -p logs", :pty => false
          run "cd #{latest_release}; mkdir -p tmp", :pty => false
        end
    end
end


namespace :deploy do
  desc "start server"
  task :start, :roles => :app do
    run "cd #{current_path} && bin/server.sh start",:pty => false
    #run "cd #{current_path} && bundle exec thin start -C config/thin/thin.faye.yml"
    # for some reason, the ssl need you to input the pem code to ssl start, this will hang up the capistrano.
    # run "#{sudo} /etc/init.d/nginx start"
  end
  
  desc "stop server"
  task :stop, :roles => :app do
    
    run "cd #{current_path} && bin/server.sh stop",:pty => false
    #run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.faye.yml"
    #run "#{sudo} /etc/init.d/nginx stop"
    #run "cd #{current_path} && /etc/init.d/thin stop"
  end

desc "restart server"
  task :restart, :roles => :app do
    run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.ewhine.yml"
    #run "cd #{current_path} && bundle exec thin stop -C config/thin/thin.faye.yml"
    run "cd #{current_path} && bundle exec thin start -C config/thin/thin.ewhine.yml"
    #run "cd #{current_path} && bundle exec thin start -C config/thin/thin.faye.yml"
    #
  end
end

desc "all update"
task :all do
  run "echo all finished!"
end

task :locale do
  run ". ~/.bash_profile && locale",:pty=>false
  run "sleep 5"
end

task :remote_backup do
  n = Time.now
  backup_timestamp = n.strftime("%Y%m%d%H%M")
  run "cd ~/backup && mysqldump -uroot -pesns esns_production | gzip > backup#{backup_timestamp}.sql.gz"
  run_locally "scp ewhine@www.gz3renxing.com:/home/ewhine/backup/backup#{backup_timestamp}.sql.gz tmp/dbbackup.sql.gz"

end



