<template>
    <DataLayout>
        <template v-slot:header>
            <!-- 创建附件 -->
            <el-dialog :title="$t('application.Import')" :visible.sync="importdialogVisible" width="70%" :close-on-click-modal="false" :append-to-body="true">
                <el-form size="mini" :label-width="formLabelWidth" v-loading='uploading'>
                    <div style="height:200px;overflow-y:scroll; overflow-x:scroll;">
                    <el-upload
                        :limit="100"
                        :file-list="fileList"
                        action
                        :on-change="handleChange"
                        :auto-upload="false"
                        :multiple="true"
                    >
                        <el-button slot="trigger" size="small" type="primary">{{$t('application.selectFile')}}</el-button>
                    </el-upload>
                    </div>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="importdialogVisible = false">{{$t('application.cancel')}}</el-button>
                    <el-button type="primary" @click="uploadData()">{{$t('application.start')+$t('application.Import')}}</el-button>
                </div>
            </el-dialog>
            <el-dialog
            :visible.sync="updateBoxVisible"
            :append-to-body="true"
            width="70%"
            >
                <ShowProperty
                ref="ShowBoxProperty"
                @onSaved="onSaved"
                width="100%"
                folderPath
                v-bind:typeName="typeName"
                :itemId="boxId"
                ></ShowProperty>
                <div slot="footer" class="dialog-footer">

          <slot name="saveButton" :data="propertiesData">
            <el-button @click="saveItem()">{{
              $t("application.save")
            }}</el-button>
          </slot>

          <el-button @click="updateBoxVisible = false">{{
            $t("application.cancel")
          }}</el-button>
        </div>
            </el-dialog>
            <el-dialog
            title="更新主文件"
            :visible.sync="udialogVisible"
            v-loading="mainFileUploading"
            :append-to-body="true"
            >
            <el-form label-position="right" label-width="120px">
                <el-form-item :label="$t('message.file')" :label-width="formLabelWidth">
                <el-upload
                    :limit="1"
                    :file-list="newFileList"
                    action
                    :on-change="handleFileChange"
                    :auto-upload="false"
                    :multiple="false"
                >
                    <el-button slot="trigger" size="small" type="primary">{{
                    $t("application.selectFile")
                    }}</el-button>
                </el-upload>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="udialogVisible = false">{{
                $t("application.cancel")
                }}</el-button>
                <el-button type="primary" @click="updateNewFile()">{{
                $t("application.ok")
                }}</el-button>
            </div>
            </el-dialog>
            <el-row>
            <ShowProperty
            v-if="allowEdit"
            ref="ShowProperty"
            @onSaved="onSaved"
            width="100%"
            folderPath
            :showUploadFile="showUploadFile"
            v-bind:typeName="typeName"
            :itemId="formId"
            ></ShowProperty>
            <ShowPropertyReadOnly
            v-else
            ref="ShowProperty"
            width="100%"
            :itemId="formId"
            :typeName="typeName"
            ></ShowPropertyReadOnly>
            </el-row>
            <el-row v-if="allowChangeDoc">
                <el-form :inline="true" :model="filters" @submit.native.prevent>
                <el-form-item>
                    <el-button type="primary" @click="showUpdateFile()">{{$t('application.Manual')+$t('application.Import')}}</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="updateDocContent()">{{$t('application.replace')}}</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="changeBoxAttrs()">修改文件的案卷属性</el-button>
                </el-form-item>
                </el-form>
            </el-row>
            
        </template>
        <template v-slot:main="{layout}">
            <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
                <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
                    <template slot="paneL">
                        <DataGrid
                            ref="mainDataGrid"
                            key="main"
                            dataUrl="/dc/getDocuByRelationParentId"
                            :parentId="formId"
                            :tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                            :isshowOption="true" 
                            :isshowSelection ="true"
                            gridViewName="ModifyDocGrid"
                            condition=" and a.name = 'irel_children' "
                            :optionWidth = "2"
                            :isshowCustom="false"
                            :isEditProperty="true"
                            showOptions="查看内容,查看属性"
                            :isShowPropertyButton="false"
                            :isShowChangeList="false"
                            @rowclick="rowClick"
                            @selectchange="selectChange"
                        ></DataGrid>
                    </template>
                    <template slot="paneR">
                        <el-tabs  v-model="selectedTabName">
                            <el-tab-pane  :label="$t('application.Attachment')" name="t03" >
                                <!--列表-->
                                <DataGrid
                                    ref="attachmentDoc"
                                    key="attachmentDocKey"
                                    dataUrl="/dc/getDocuByRelationParentId"
                                    v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                                    v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                                    gridViewName="ChangeAttachmentGrid"
                                    condition=" and a.NAME='irel_children'"
                                    :optionWidth = "2"
                                    :isshowCustom="false"
                                    :isEditProperty="false"
                                    :isShowMoreOption="true"
                                    showOptions="查看内容,查看属性"
                                    :isShowChangeList="false"
                                    @selectchange="attachmentDocSelect"
                                ></DataGrid>
                            </el-tab-pane>
                        </el-tabs>
                    </template>
                </split-pane>
            </div>
        </template>
    </DataLayout>
</template>

<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import ShowPropertyReadOnly from "@/components/ShowPropertyReadOnly.vue";
import DataGrid from "@/components/DataGrid";
import BatchImport from '@/components/controls/ImportDocument';
import DataSelect from '@/components/ecm-data-select'
import MountFile from '@/components/MountFile.vue';
import AttachmentFile from "@/views/dc/AttachmentFile.vue"
import DataLayout from '@/components/ecm-data-layout'
export default {
    // CNPE 待提交文函
    data(){
        return{
            // 本地存储高度名称
            topStorageName: 'SubmissiondcHeight',
            // 非split pan 控制区域高度
            startHeight: 135,
            // 顶部百分比*100
            topPercent: 65,
            // 顶部除列表高度
            topbarHeight: 35,
            // 底部除列表高度
            bottomHeight: 80,
            formLabelWidth: "120px",
            buttLoading:false,
            filters: {
                projectCode: "",
                docType: "",
                coding: "",
                title: "",
                limit: 10,
                typeName:'',
                relationName:'',
            },
            dialog:{
                title:"",
                visible:false
            },
            typeName:"文件传递单",
            mainFileUploading:false,
            selectedItems: [],
            udialogVisible: false,
            childrenTypeSelectVisible:false,
            selectedChildrenType:'',
            selectedTransferDocItems:[],
            newFileList: [],
            parentId:"",
            selectRow:[],
            relationName:"",
            uploadFile: null,
            importdialogVisible:false,
            fileList: [],
            uploading:false,
            selectedAttachment:[],
            uploadUrl:'',
            gridObj:[],
            rightTableHeight: (window.innerHeight - 60)/2,
            relation:{},
            isShowRelevant:true,
            selectedTabName:'t03',
            docId:"",
            isOnly:false,
            attachmentId:"",
            boxId:"",
            updateBoxVisible:false
        }
    },
    created(){
        
    },
    mounted(){
        if(!this.validataPermission()){
            //跳转至权限提醒页
            let _self=this;
            _self.$nextTick(()=>{
                _self.$router.push({ path: '/NoPermission' })
            })
        }
        setTimeout(() => {
            this.topPercent = this.getStorageNumber(this.topStorageName,60)
        }, 300);
    },
    methods: {
        // 上下分屏事件
        onSplitResize(topPercent){
            // 顶部百分比*100
            this.topPercent = topPercent
            this.setStorageNumber(this.topStorageName, topPercent)
            //console.log(JSON.stringify(topPercent))
        },
        dbClick(row){
            this.docId=row.ID;
            this.dialog.visible=true;
            
            this.$nextTick(()=>{
                this.$refs.subAttachment.refresh();
                // this.$refs.subAttachment.docId=row.ID;
            });
        },
        attachmentDocSelect(val){
            this.selectedAttachment=val;
        },
        handleChange(file, fileList) {
            this.fileList = fileList;
        },
        getFormData() {
            let _self = this;
            let formdata = new FormData();
            var data = {};
            data["parentDocId"] = _self.parentId;//_self.selectedInnerItems[0].ID;//_self.selectedFileId;
            data["relationName"]='irel_children';
            data["TYPE_NAME"]='附件';
            formdata.append("metaData", JSON.stringify(data));
            _self.fileList.forEach(function(file) {
                //console.log(file.name);
                formdata.append("uploadFile", file.raw, file.name);
            });
            return formdata;
            },
        handleFileChange(file, fileList) {
            console.log(file)
        this.uploadFile = file;
        },
        //通过传递父id和附件id更新文档的主文件
        updateDocContent(){
            let _self = this;
            if ( _self.selectedItems==undefined || _self.selectedItems.length<1) {
                _self.$message({
                    showClose: true,
                    message: _self.$t('message.PleaseSelectOneFile'),
                    duration: 2000,
                    type: "warning"
                    });
                    return;
            }
            var m = new Map();
            let formdata = new FormData();
            m.set("primaryId",_self.selectedItems[0].ID)
            m.set("contentId",_self.attachmentId)
            formdata.append("metaData",JSON.stringify(m))
            _self
                .axios({
                headers: {
                    "Content-Type": "application/json;charset=UTF-8"
                },
                datatype: "json",
                method: "post",
                data: formdata,
                url:'/dc/updateDcContentById'
                })
                .then(function(data){
                    if(data.data.code=="1"){
                        _self.$message({
                        showClose: true,
                        message: _self.$t('application.update')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'success'
                    });
                        // _self.$message("更新成功!");
                    }else{
                        _self.$message({
                        showClose: true,
                        message: _self.$t('application.update')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'warning'
                    });
                    }
                   
                })
                .catch(function(error) {
                _self.$message({
                        showClose: true,
                        message: _self.$t('application.update')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'warning'
                    });
                _self.uploading=false;
                console.log(error);
                });
            
        },
        //上传文件
        uploadData() {
            let _self = this;
            let formdata = _self.getFormData();
            _self.uploading=true;
            _self
                .axios({
                headers: {
                    "Content-Type": "application/json;charset=UTF-8"
                },
                datatype: "json",
                method: "post",
                data: formdata,
                url: _self.uploadUrl
                })
                .then(function(response) {
                _self.importdialogVisible = false;
                _self.uploading=false;
                _self.$refs.attachmentDoc.loadGridData();
                _self.$message({
                        showClose: true,
                        message: _self.$t('application.Import')+_self.$t('message.success'),
                        duration: 2000,
                        type: 'success'
                    });
                })
                .catch(function(error) {
                _self.uploading=false;
                console.log(error);
                });
            },
        rowClick(row){
            console.log(row)
            this.selectRow=row;
            this.parentId=row.ID;
            let _self=this;
            _self.attachmentId = "";
            _self.getAttachmentFile(this.parentId)
            _self.$nextTick(()=>{
                    _self.$refs.attachmentDoc.parentId=row.ID;
                    _self.$refs.attachmentDoc.loadGridData();
               });
        },
        getAttachmentFile(parentId){
            let _self = this;
             var m = new Map();
             m.set("id",parentId)
             m.set("condition","and a.name = 'irel_children' ")
             m.set("pageSize", 1);
             m.set("pageIndex", 0);
             m.set("orderBy", "")
            axios
            .post("/dc/getDocuByRelationParentId", JSON.stringify(m))
            .then(function (response) {
                _self.attachmentId = response.data.data[0].ID
            })
            .catch(function (error) {
                console.log(error);
            });
        },
        showUpdateFile(indata) {
            let _self = this;
            if (this.selectedItems && this.selectedItems.length > 0) {
            this.uploadFile = [];
            this.udialogVisible = true;
            }else{
            _self.$message({
                showClose: true,
                message: _self.$t('message.PleaseSelectOneFile'),
                duration: 2000,
                type: "warning"
                });
                return;
            }
        },
        // 表格行选择
        selectChange(val) {
            this.selectedItems = val;
        },
        updateNewFile() {
            let _self = this;
            if (_self.selectedItems && _self.selectedItems.length > 0) {
            _self.uploadPrimry();
          }
        },
        uploadPrimry() {
            let _self = this;
            _self.mainFileUploading = true;
            let formdata = new FormData();
            formdata.append("id", _self.selectedItems[0].ID);
            if (_self.uploadFile != "") {
            formdata.append("uploadFile", _self.uploadFile.raw);
            }
            axios
            .post("/dc/updatePrimaryContent", formdata, {
                "Content-Type": "multipart/form-data",
            })
            .then(function (response) {
                _self.udialogVisible = false;
                _self.$message("更新成功!");
                _self.mainFileUploading = false;
            })
            .catch(function (error) {
                _self.$message("更新失败!");
                console.log(error);
                _self.mainFileUploading = false;
            });
        },
        changeBoxAttrs(){
            let _self = this;
            if ( _self.selectedItems==undefined || _self.selectedItems.length<1) {
                _self.$message({
                    showClose: true,
                    message: _self.$t('message.PleaseSelectOneFile'),
                    duration: 2000,
                    type: "warning"
                    });
                    return;
            }
            var m = new Map();
            m.set("childId",_self.selectedItems[0].ID)
            axios
            .post("/dc/getBoxDocByChildId", JSON.stringify(m))
            .then(function (response) {
                if(response.data.code == "1"){
                    if(response.data.isBox){
                        _self.updateBoxVisible = true;
                        _self.boxId = response.data.boxId;
                        _self.$nextTick(() => {
                        _self.$refs.ShowBoxProperty.loadFormInfo();
                        });
                    }else{
                        _self.$message({
                            showClose: true,
                            message: "此文件不包含卷",
                            duration: 2000,
                            type: "warning"
                            });
                    }
                }
            })
            .catch(function (error) {
                console.log(error);
            });
        },
        saveItem(){
            this.$refs.ShowBoxProperty.saveItem();
            this.updateBoxVisible = false;
        }
    },
    props: {
        formId: {type: String,default: ""},
        allowEdit: { type: Boolean, default: false },
        showUploadFile: { type: Boolean, default: true },
        processDefinitionId: { type: String, default: "" },
        activityName: { type: String, default: "" },
        allowChangeDoc: { type: Boolean, default: true }
    },
    components: {
        ShowProperty:ShowProperty,
        ShowPropertyReadOnly: ShowPropertyReadOnly,
        DataGrid:DataGrid,
        DataSelect:DataSelect,
        BatchImport:BatchImport,
        MountFile:MountFile,
        AttachmentFile:AttachmentFile,
        DataLayout:DataLayout
    }
}
</script>
<style scoped>

</style>