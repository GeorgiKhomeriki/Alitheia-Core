/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.impl.service.web.services.utils;

interface ProjectManagerDBQueries {

    public static final String GET_EVALUATED_PROJECTS = "select distinct sp " +
                                                        "from StoredProject sp, EvaluationMark em " +
                                                        "where sp.id=em.storedProject ";
    
    
    public static final String GET_FILES_BY_PROJECT_ID_PARAM = "project_id";
    
    public static final String GET_FILES_BY_PROJECT_ID = "select distinct pf " +
                                                    "from ProjectVersion pv, ProjectFile pf " +
                                                    "where pf.projectVersion=pv.id " +
                                                    " and pv.project.id=:" +
                                                    GET_FILES_BY_PROJECT_ID_PARAM;
    
    
    public static final String GET_FILES_BY_PROJECT_VERSION_ID_PARAM = "project_ver";
    
    public static final String GET_FILES_BY_PROJECT_VERSION_ID = "select pf.project_file_id, d.directory_id, " + 
                                                                 "       head.fname, head.headrev, pf.file_status, pf.is_directory " +
    		                                                     "from (select pf.directory_id as dir, " +
    		                                                     "             pf.file_name as fname, " +
    		                                                     "             max(pv.project_version_id) as headrev " +
    		                                                     "      from project_file pf, project_version pv " +
    		                                                     "      where pf.project_version_id=pv.project_version_id " +
    		                                                     "            and pv.timestamp<= ( " +
    		                                                     "                select pv2.timestamp " +
    		                                                     "                from project_version pv2 " +
    		                                                     "                where pv2.project_version_id=:" +
    		                                                     GET_FILES_BY_PROJECT_VERSION_ID_PARAM +
    		                                                     "                ) " +       
    		                                                     "      group by pf.directory_id, pf.file_name) head," +
    		                                                     "      project_file pf, directory d " +
    		                                                     "where d.directory_id=pf.directory_id " +
    		                                                     "      and head.dir=pf.directory_id " +
    		                                                     "      and head.fname=pf.file_name " +
    		                                                     "      and pf.project_version_id=head.headrev " +
    		                                                     "      and pf.file_status<>'DELETED' " +
    		                                                     "order by d.path, head.fname";
    
    
    public static final String GET_FILE_GROUPS_BY_PROJECT_ID_PARAM = "project_id";
    
    public static final String GET_FILE_GROUPS_BY_PROJECT_ID = "select fg " +
                                                               "from ProjectVersion pv, FileGroup fg " +
                                                               "where fg.projectVersion=pv.id " +
                                                               " and pv.project.id=:" +
                                                               GET_FILE_GROUPS_BY_PROJECT_ID_PARAM;
    
    
    public static final String GET_FILES_NUMBER_BY_PROJECT_VERSION_ID_PARAM = "project_ver";
    
    public static final String GET_FILES_NUMBER_BY_PROJECT_VERSION_ID = "select count(*) " +
                                                                    "from (select pf.directory_id as dir, " +
                                                                    "             pf.file_name as fname, " +
                                                                    "             max(pv.project_version_id) as headrev " +
                                                                    "      from project_file pf, project_version pv " +
                                                                    "      where pf.project_version_id=pv.project_version_id " +
                                                                    "            and pv.timestamp<= ( " +
                                                                    "                select pv2.timestamp " +
                                                                    "                from project_version pv2 " +
                                                                    "                where pv2.project_version_id=:" +
                                                                    GET_FILES_BY_PROJECT_VERSION_ID_PARAM +
                                                                    "                ) " +       
                                                                    "      group by pf.directory_id, pf.file_name) head," +
                                                                    "      project_file pf, directory d " +
                                                                    "where d.directory_id=pf.directory_id " +
                                                                    "      and head.dir=pf.directory_id " +
                                                                    "      and head.fname=pf.file_name " +
                                                                    "      and pf.project_version_id=head.headrev " +
                                                                    "      and pf.file_status<>'DELETED' ";
    
    
    public static final String GET_DIRECTORIES_BY_IDS_PARAM = "list_of_dirs_ids";
    
    public static final String GET_DIRECTORIES_BY_IDS = "select dir " +
    		                                            "from Directory dir " +
    		                                            "where dir.id in (:" +
    		                                            GET_DIRECTORIES_BY_IDS_PARAM + ") ";
    
    
    public static final String GET_DEVELOPERS_BY_IDS_PARAM = "list_of_devs_ids";
    
    public static final String GET_DEVELOPERS_BY_IDS = "select dev " +
    		                                           "from Developer dev " +
    		                                           "where dev.id in (:" +
    		                                           GET_DEVELOPERS_BY_IDS_PARAM + ") ";
    
    
    public static final String GET_PROJECTS_BY_IDS_PARAM = "list_of_sps_ids";
    
    public static final String GET_PROJECTS_BY_IDS = "select sp " +
                                                     "from StoredProject sp " +
                                                     "where sp.id in (:" +
                                                     GET_PROJECTS_BY_IDS_PARAM + ") ";
    
    
    public static final String GET_PROJECT_VERSIONS_BY_IDS_PARAM = "list_of_pvs_ids";
    
    public static final String GET_PROJECT_VERSIONS_BY_IDS = "select pv " +
                                                             "from ProjectVersion pv " +
                                                             "where pv.id in (:" +
                                                             GET_PROJECT_VERSIONS_BY_IDS_PARAM + ") ";
    
    
    public static final String GET_PROJECT_VERSIONS_BY_VERSION_NUMBERS_PARAM_PR_ID  = "project_id";
    
    public static final String GET_PROJECT_VERSIONS_BY_VERSION_NUMBERS_PARAM_VB_IDS = "list_of_ver_numbers";
    
    public static final String GET_PROJECT_VERSIONS_BY_VERSION_NUMBERS = "select pv " +
    		                                                             "from ProjectVersion pv " +
    		                                                             "where pv.version in (:" +
    		                                                             GET_PROJECT_VERSIONS_BY_VERSION_NUMBERS_PARAM_VB_IDS + ") " +
    		                                                             " and pv.project.id=:" +
    		                                                             GET_PROJECT_VERSIONS_BY_VERSION_NUMBERS_PARAM_PR_ID;
    
    
    public static final String GET_LAST_PROJECT_VERSIONS_PARAM = "list_of_sps_ids";
    
    public static final String GET_LAST_PROJECT_VERSIONS = "select pv " +
    		                                               "from ProjectVersion pv " +
    		                                               "where pv.project.id in (:" +
    		                                               GET_LAST_PROJECT_VERSIONS_PARAM + ") " +
    		                                               " and pv.version= " +
    		                                               "      (select max(pv1.version) " +
    		                                               "      from ProjectVersion pv1 " +
    		                                               "      where pv1.project = :" + 
    		                                               GET_LAST_PROJECT_VERSIONS_PARAM + ")";
    
}

//vi: ai nosi sw=4 ts=4 expandtab
