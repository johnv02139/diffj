require 'rubygems'
require 'rake'
require 'java'
require 'rake/testtask'
require 'rake/packagetask'

include Java

require 'ant'

DIFFJ_VERSION    = "1.3.0"
$diffj_name      = "diffj"
$diffj_fullname  = "#{$diffj_name}-#{DIFFJ_VERSION}"

# directories - Gradle/Maven layout (mostly)

$src_dir            = 'src'
$src_main_dir       = $src_dir      + '/main'
$src_main_java_dir  = $src_main_dir + '/java'
$src_main_ruby_dir  = $src_main_dir + '/ruby'
$src_main_jruby_dir = $src_main_dir + '/jruby'

$src_test_dir       = $src_dir      + '/test'
$src_test_java_dir  = $src_test_dir + '/java'
$src_test_ruby_dir  = $src_test_dir + '/ruby'

$staging_dir        = 'staging'
$staging_cls_dir    = $staging_dir + '/classes'

directory $staging_cls_main_dir   = $staging_cls_dir + '/main'
directory $staging_cls_test_dir   = $staging_cls_dir + '/test'
directory $staging_cls_jruby_dir  = $staging_cls_dir + '/jruby'

directory $staging_report_dir     = $staging_dir + '/report'

# this is the destination of the Java-only jarfile:
directory $staging_libs           = $staging_dir + '/libs'

directory $staging_dist_dir       = $staging_dir + "/dist"
directory $staging_dist_diffj_dir = $staging_dist_dir + "/#{$diffj_fullname}"

directory $staging_dist_bin_dir   = $staging_dist_diffj_dir + '/bin'
directory $staging_dist_lib_dir   = $staging_dist_diffj_dir + '/lib/' + $diffj_name

$libs_dir           = 'libs'
$jruby_complete_jar = 'libs/jruby-complete-1.6.3.jar'
$pmd_jar            = 'libs/pmd-4.2.5.jar'
$junit_jar          = 'libs/junit-4.10.jar'

# we're still using this, for JRuby vs. Java tests:
$diffj_java_jar     = "staging/libs/#{$diffj_fullname}.jar"

# this is the full JRuby jarfile, which will replace the above:
$diffj_jruby_jar    = "#{$diffj_fullname}.jar"

# this is fixed in JRuby 1.6.0:
$CLASSPATH << "#{ENV['JAVA_HOME']}/lib/tools.jar"

# This doesn't seem to work. If $diffj_java_jar doesn't exist when the Rakefile
# is executed, java:jar is executed, but the jruby:tests task fails with an
# error that the DiffJ Java code can't be found. But the next time jruby:tests
# runs (with the diffj jarfile existing now), it runs successfully.

$CLASSPATH << $diffj_java_jar
$CLASSPATH << $jruby_complete_jar
$CLASSPATH << $pmd_jar

buildjars = [ $jruby_complete_jar, $pmd_jar ]
testjars =  [ $junit_jar ]

# Ant code to build Java

task :setup do
  ant.path :id => 'classpath' do
    buildjars.each do |jarfile|
      fileset :file => jarfile
    end
  end

  ant.path :id => 'test.classpath' do
    pathelement :location => $staging_cls_main_dir
    path        :refid    => 'classpath'
    testjars.each do |jarfile|
      fileset :file => jarfile
    end
    pathelement :location => $staging_cls_test_dir
  end
end

task 'java:compile' => [ :setup, $staging_cls_main_dir ] do
  ant.javac(:destdir => $staging_cls_main_dir, 
            :srcdir => $src_main_java_dir,
            :classpathref => 'classpath',
            :debug => 'yes',
            :includeantruntime => 'no')
end

task 'java:tests:compile' => [ :setup, $staging_cls_test_dir, 'java:compile' ] do
  ant.javac(:destdir => $staging_cls_test_dir, 
            :srcdir => $src_test_java_dir,
            :classpathref => 'test.classpath',
            :debug => 'yes',
            :includeantruntime => 'no')
end

# this should depend on the tests, but sometimes I create a jar for
# field testing, which won't yet pass the tests.
desc "Build the jarfile for the Java code (no JRuby)"
task "java:jar" => [ "java:compile", $staging_libs ] do
  ant.jar(:jarfile => $diffj_java_jar, 
          :basedir => $staging_cls_main_dir)
end

desc "Run the Java tests"
task "java:tests" => [ "java:tests:compile", $staging_report_dir ] do  
  ant.junit(:fork => "yes", :forkmode => "once", :printsummary => "yes",  
            :showoutput => true,
            :haltonfailure => "no", :failureproperty => "tests.failed") do  
    classpath :refid => 'test.classpath'  
    formatter :type => "xml"
    formatter :type => "plain"
    batchtest :todir => $staging_report_dir do  
      fileset :dir => $src_test_java_dir, :includes => '**/Test*.java'  
    end  
  end  
  if ant.project.getProperty "tests.failed"
    ant.junitreport :todir => $staging_report_dir do  
      fileset :dir => $staging_report_dir, :includes => "TEST-*.xml"  
      report :todir => "#{$staging_report_dir}/html"  
    end  
    ant.fail :message => "Test(s) failed. Report is at #{$staging_report_dir}/html."
  end  
end

# JRuby tasks:

desc "Compile the JRuby code"
task "jruby:compile" => [ :setup, $staging_cls_main_dir ] do
  ant.javac(:destdir => $staging_cls_main_dir, 
            :srcdir => $src_main_jruby_dir,
            :classpathref => 'classpath',
            :debug => 'yes',
            :includeantruntime => 'no')
end

desc "Build the jarfile including JRuby and PMD"
task "jruby:jar" => [ "java:compile", "jruby:compile" ] do
  cmd  = "jar -cfm #{$diffj_jruby_jar} src/main/jar/launcher.manifest "
  cmd << "-C #{$staging_cls_main_dir} org/incava/diffj/DiffJLauncher.class "
  # this is PMD and JRuby combined, since jar whines about duplicate directories (such as "org"):
  cmd << "-C vendor/all . "
  cmd << "-C #{$src_main_ruby_dir} . "
  sh cmd
end

class DiffJRakeTestTask < Rake::TestTask
  def initialize name, filter = name
    super(('test:' + name) => [ "java:tests:compile", "java:jar" ]) do |t|
      t.libs << $src_main_ruby_dir
      t.libs << $src_test_ruby_dir
      t.pattern = "#{$src_test_ruby_dir}/**/#{filter}/**/test*.rb"
      t.warning = true
      t.verbose = true
    end
  end
end

DiffJRakeTestTask.new 'all', '*'
DiffJRakeTestTask.new 'imports'
DiffJRakeTestTask.new 'ctor'
DiffJRakeTestTask.new 'field'
DiffJRakeTestTask.new 'method'
DiffJRakeTestTask.new 'type'
DiffJRakeTestTask.new 'types'
DiffJRakeTestTask.new 'method/body/zeroone'
DiffJRakeTestTask.new 'method/parameters/zeroone'
DiffJRakeTestTask.new 'method/throws/zeroone'
DiffJRakeTestTask.new 'method/parameters/reorder'
DiffJRakeTestTask.new 'method/parameters/reorder/typechange'

task "jruby:tests" => [ "test:all" ]

desc "Distribution"
task "dist" => [ "java:jar", $staging_dist_bin_dir, $staging_dist_lib_dir ] do
  cp "src/main/sh/diffj", $staging_dist_bin_dir
  cp $diffj_jruby_jar, $staging_dist_lib_dir
  origdir = Dir.pwd
  cd $staging_dist_dir
  sh "zip -r #{$diffj_fullname}.zip #{$diffj_fullname}"
  cd origdir
end

desc "Build Debian package"
task "debian:dist" => [ "dist" ] do
  cd $staging_dist_dir + '/' + $diffj_fullname
  rm "../diffj_#{DIFFJ_VERSION}_all.deb"
  url = "http://www.incava.org/projects/diffj"
  desc = "Java-aware file comparator"
  maint = "jeugenepace at gmail dot com"
  cmd = Array.new
  cmd << "fpm"
  cmd << "-s" << "dir"
  cmd << "-t" << "deb"
  cmd << "--name" << "diffj"
  cmd << "--version" << DIFFJ_VERSION
  cmd << "--prefix" << "usr"
  cmd << "--architecture" << "all"
  cmd << "--package" << "../diffj_#{DIFFJ_VERSION}_all.deb"
  cmd << "--maintainer" << "jeugenepace at gmail dot com"
  
  cmd << "."
  sh cmd.join(' ')
end

# todo:

# add gem install riel
# dependencies from Rake on Gradle
