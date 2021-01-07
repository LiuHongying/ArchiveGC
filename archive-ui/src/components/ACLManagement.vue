<template>
  <div>
      <el-dialog 
       width="60%"
      title="添加角色"
      :visible.sync="roleVisible"
      @close="roleVisible = false"
      :close-on-click-modal="false"
      >
        <RoleSelectComponent ref="roleSelect" v-model="roles" :maxCount="500" :isRepeat="true"></RoleSelectComponent>
        <div slot="footer" class="dialog-footer">
            <el-button @click="addRole">{{$t('application.save')}}</el-button>
            <el-button @click="roleVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>

      <el-dialog 
       width="60%"
      title="添加人员"
      :visible.sync="userRoleVisible"
      @close="userRoleVisible = false"
      :close-on-click-modal="false"
      >
        <UserSelectComponent v-model="users" ref="userSelect" :maxCount="500" :isRepeat="true"></UserSelectComponent>
        <div slot="footer" class="dialog-footer">
            <el-button @click="addUser">{{$t('application.save')}}</el-button>
            <el-button @click="userRoleVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
    <el-dialog
      width="96%"
      title="授权管理"
      :visible.sync="aclManagementVisible"
      @close="aclManagementVisible = false"
      :close-on-click-modal="false"
    >
        <el-row>
            <el-col :span="3">
                <el-button type="primary" @click="roleVisible=true">添加角色</el-button>
            </el-col>
            <el-col :span="3">
                <el-button type="primary" @click="userRoleVisible=true">添加人员</el-button>
            </el-col>
        </el-row>
        <el-row>
            <el-table :data="permitList" border ref="multipleTable" style="width: 100%">
                <el-table-column :label="$t('ID')" prop="id" width="100" v-if="show"></el-table-column>
                <el-table-column :label="$t('field.indexNumber')" type="index" width="40"></el-table-column>
                <el-table-column :label="$t('field.type')" width="50">
                <template slot-scope="scope">
                    <i v-if="scope.row.targetType==1" class="el-icon-user" :title="$t('application.user')"></i>
                    <i
                    v-if="scope.row.targetType==2"
                    class="iconfont zisecm-friend"
                    :title="$t('application.role')"
                    ></i>
                </template>
                </el-table-column>
                <el-table-column :label="$t('field.name')" prop="targetName" width="100"></el-table-column>

                <!--<el-table-column prop="date" label="日期"></el-table-column>
                        
                <el-table-column prop="address" label="地址"></el-table-column>-->
                <template v-for="(item,index) in tableHeaderData">
                    <el-table-column :key="item.tableHeadCode" width="100">
                        <template slot="header" slot-scope="scope">
                        <el-checkbox
                            :indeterminate="isIndeterminate[index]"
                            v-model="checkAll[index]"
                            :ref="'headerCheck_'+index"
                            @change="((val) => handleCheckAllChange(val,index))"
                        >{{item.tableHeaderName}}</el-checkbox>
                        </template>

                        <template slot-scope="scope">
                        <!-- <el-checkbox-group v-model="checkedOptions" @change="handlecheckedOptionsChange(checked,scope.row,index)">
                                                <el-checkbox :label="scope.row.gameId" >选中</el-checkbox>
                        </el-checkbox-group>-->
                        <el-checkbox
                            
                            v-model="checkedOptions[index][scope.$index]"
                            @change="checked =>handlecheckedOptionsChange(checked,scope.row,scope.$index,index)"
                        ></el-checkbox>

                        <span v-if="item.permission==='1'">{{$t('application.objectPermission1')}}</span>
                        <span v-if="item.permission==='2'">{{$t('application.objectPermission2')}}</span>
                        <span v-if="item.permission==='3'">{{$t('application.objectPermission3')}}</span>
                        <span v-if="item.permission==='4'">{{$t('application.objectPermission4')}}</span>
                        <span v-if="item.permission==='5'">{{$t('application.objectPermission5')}}</span>
                        <span v-if="item.permission==='6'">{{$t('application.objectPermission6')}}</span>
                        <span v-if="item.permission==='7'">{{$t('application.objectPermission7')}}</span>
                        <span v-if="item.permission==='8'">{{$t('application.objectPermission8')}}</span>
                        <span v-if="item.permission==='9'">{{$t('application.objectPermission9')}}</span>
                        <!--  <span v-if="item.tableHeaderCode==='download'">下载权限1</span>
                        <span v-if="item.tableHeaderCode==='delete'">删除权限1</span>-->
                        </template>
                    </el-table-column>
                
                </template>
                <el-table-column :key="xd" width="100">
                    <template slot="header">
                        操作
                    </template>
                    <template slot-scope="scope" v-if="scope.row.targetName!='owner'&&scope.row.targetName!='everyone'">
                        <el-button
                            plain
                            icon="el-icon-info"
                            type="danger"
                            @click="deleteRow(scope.row)"
                        >删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <!-- <div style="margin-top: 20px; float:right "> -->
                <!-- <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">全选</el-checkbox>-->
                <!-- <el-button type="primary" @click="saveButton(form)">{{$t('application.ok')}}</el-button> -->
            <!-- </div> -->
        </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button @click="beforeSave">{{$t('application.save')}}</el-button>
        <el-button @click="aclManagementVisible = false">{{$t('application.cancel')}}</el-button>
      </div>
    </el-dialog>
    <el-button icon="el-icon-info" type="primary" @click="aclManagementVisible=true">
      <slot>授权</slot>
    </el-button>
  </div>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import DataGrid from "@/components/DataGrid";
import UserSelectComponent from "@/components/controls/UserSelectComponent";
import RoleSelectComponent from "@/components/controls/RoleSelectComponent";
export default {
  name: "aclManagement",
  components: {
      DataGrid:DataGrid,
      DataLayout:DataLayout,
      UserSelectComponent:UserSelectComponent,
      RoleSelectComponent:RoleSelectComponent
  },
  data() {
    return {
      aclManagementVisible: false,
      show: false,
      aclData: "", //Acl信息
      aclId: "",
      inputKey: "",
      permitList: [],
      permitListFull: [],
      //  checkAll: false,
      //  isIndeterminate: false,
      checkedOptions: [], //列选择项 选中控制 二维
      tableHeaderData: [],
      tableData: [],

      multipleSelection: [],
      checkedGameId: [],

      checkAllOptions: [], //全选备用赋值数组   二维  注意没写错 这里暂时先定义成一维数组 业务需求 需要动态验证是否选中
      noCheckAllOptions: [], //全选备用赋值数组 二维
      checkOptionCount: [], //实时记录当前列 选中数量
      checkAll: [], //表头全选按钮 选中控制
      isIndeterminate: [], //表头 不确定状态控制
      userRoleVisible:false,
      activeName:'first',
      users:"",
      roleVisible:false,
      roles:""
    };
  },
  props: {
    ids: {
      type: Array,
      default: function() {
        return new Array();
      }
    },
    isFolder:{type:Boolean,default:false}
  },
  mounted() {
    this.initCheckBoxData();
  },
  methods: {
      beforeSave(){
            let _self=this;
            if(_self.isFolder){
                _self.$confirm(
            "是否同时修改文件夹内所有对象",
            _self.$t("application.info"),
            {
                confirmButtonText: _self.$t("application.ok"),
                cancelButtonText: _self.$t("application.cancel"),
                type: "warning"
            }
            )
            .then(() => {
                _self.saveItem(_self.isFolder,true);
            })
            .catch(() => {
                _self.saveItem(_self.isFolder,false);
            });
            }else{
                 _self.saveItem(_self.isFolder,false);
            }
            
      },
      saveItem(isFolder,isUpdateChild){
            let data=new Map();
            let _self=this;
            let dataList=new Array();
            for(let x=0;x<_self.permitList.length;x++){
                let row=new Map();
                let obj= _self.permitList[x];
                row.set("targetName",obj.targetName);
                row.set("targetType",obj.targetType);
                
                for(let n=0;n<_self.tableHeaderData.length;n++){

                    if (n == 0) {
                        if (_self.checkedOptions[n][x]) {
                        //console.log( this.$t('application.objectPermission1'));
                        row.set("permission", 1);
                        }
                    } else if (n == 1) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 2);
                        }
                    } else if (n == 2) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 3);
                        }
                    } else if (n == 3) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 4);
                        }
                    } else if (n == 4) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 5);
                        }
                    } else if (n == 5) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 6);
                        }
                    } else if (n == 6) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 7);
                        }
                    } else if (n == 7) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 8);
                        }
                    } else if (n == 8) {
                        if (_self.checkedOptions[n][x]) {
                        row.set("permission", 9);
                        }
                    }

                }
                if(row.get("permission")==null||row.get("permission")==undefined){
                    row.set("permission", 1);
                }
                dataList.push(row)
            }
            data.set("objIds",_self.ids);
            data.set("isUpdateChild",isUpdateChild);
            data.set("data",dataList);
            if(_self.isFolder){
                //foder
                _self.axios({
                    headers: {
                        "Content-Type": "application/json;charset=UTF-8",
                    },
                    method: "post",
                    data: JSON.stringify(data),
                    url: "/folder/grantPermitBatch",
                    })
                    .then(function (response) {
                    if (response.data.code == 1) {
                        _self.$message({
                        showClose: true,
                        message: "授权成功",
                        duration: 2000,
                        type: "Success",
                        });
                        _self.aclManagementVisible=false;
                        _self.$emit("grantSuccess");
                    }else{
                        _self.$message({
                            showClose: true,
                            message: "授权失败",
                            duration: 2000,
                            type: "error",
                            });
                        _self.$emit("grantFaild",response.data.data);
                    }
                    })
                    .catch(function (error) {
                        _self.$emit("grantFaild","授权失败："+error);
                        console.log(error);
                    });
            }else{
                //文件
                _self.axios({
                    headers: {
                        "Content-Type": "application/json;charset=UTF-8",
                    },
                    method: "post",
                    data: JSON.stringify(data),
                    url: "/dc/grantPermitBatchDoc",
                    })
                    .then(function (response) {
                    if (response.data.code == 1) {
                        _self.$message({
                        showClose: true,
                        message: "授权成功",
                        duration: 2000,
                        type: "success",
                        });
                        _self.aclManagementVisible=false;
                        _self.$emit("grantSuccess");
                    }else{
                         _self.$message({
                            showClose: true,
                            message: "授权失败",
                            duration: 2000,
                            type: "error",
                            });
                        _self.$emit("grantFaild",response.data.data);
                    }
                    })
                    .catch(function (error) {
                        _self.$emit("grantFaild","授权失败："+error);
                        console.log(error);
                    });
            }
      },
      deleteRow(row){
          let _self=this;
          _self.permitList.forEach((e,index)=>{
              if(e.targetName==row.targetName){
                _self.permitList.splice(index, 1);
                for(let x=0;x<_self.tableHeaderData.length;x++){
                    _self.checkedOptions[x].splice(index, 1);
                    _self.checkAllOptions[x].splice(index, 1);
                  _self.noCheckAllOptions[x].splice(index, 1);
                }
                
              }
          });
          _self.permitList=JSON.parse(JSON.stringify(_self.permitList));
          _self.checkedOptions=JSON.parse(JSON.stringify(_self.checkedOptions));
          _self.checkAllOptions=JSON.parse(JSON.stringify(_self.checkAllOptions));
          _self.noCheckAllOptions=JSON.parse(JSON.stringify(_self.noCheckAllOptions));
      },
      addRole(){
          this.$refs.roleSelect.addToParent();
          
          console.log(this.roles);
          this.roleVisible=false;
          let rolesList= this.roles.split(";");
          let rolesObjList=new Array();
          let _self=this;
    //       _self.permitList = [
    //     { targetType: "1", targetName: "everyone" },
    //     { targetType: "1", targetName: "owner" }
    //   ];
          rolesList.forEach((e)=>{
              let haveRole=false;
              _self.permitList.forEach((x)=>{
                  if(x.targetName==e){
                      haveRole=true;
                  }
              })
              if(!haveRole){
                  _self.permitList.push( { "targetType": "2", "targetName": e });
              }
              
          });
          _self.permitList=JSON.parse(JSON.stringify(_self.permitList));
          for(let x=0;x<_self.tableHeaderData.length;x++){
              for(let n=0;n<_self.permitList.length;n++){
                  _self.checkedOptions[x][n]=false;
                  _self.checkAllOptions[x][n] = true; // 全选值 赋值  true
                  _self.noCheckAllOptions[x][n] = false;
              }
          }
          _self.checkedOptions=JSON.parse(JSON.stringify(_self.checkedOptions));
          _self.checkAllOptions=JSON.parse(JSON.stringify(_self.checkAllOptions));
          _self.noCheckAllOptions=JSON.parse(JSON.stringify(_self.noCheckAllOptions));
          
      },
      addUser(){
          this.$refs.userSelect.addToFather();
          
          console.log(this.users);
          this.userRoleVisible=false;
          let userList= this.users.split(";");
          let userObjList=new Array();
          let _self=this;
    //       _self.permitList = [
    //     { targetType: "1", targetName: "everyone" },
    //     { targetType: "1", targetName: "owner" }
    //   ];
          userList.forEach((e)=>{
              let haveUser=false;
              _self.permitList.forEach((x)=>{
                  if(x.targetName==e){
                      haveUser=true;
                  }
              })
              if(!haveUser){
                  _self.permitList.push( { "targetType": "1", "targetName": e });
              }
              
          });
          _self.permitList=JSON.parse(JSON.stringify(_self.permitList));
          for(let x=0;x<_self.tableHeaderData.length;x++){
              for(let n=0;n<_self.permitList.length;n++){
                  _self.checkedOptions[x][n]=false;
                  _self.checkAllOptions[x][n] = true; // 全选值 赋值  true
                  _self.noCheckAllOptions[x][n] = false;
              }
          }
          _self.checkedOptions=JSON.parse(JSON.stringify(_self.checkedOptions));
          _self.checkAllOptions=JSON.parse(JSON.stringify(_self.checkAllOptions));
          _self.noCheckAllOptions=JSON.parse(JSON.stringify(_self.noCheckAllOptions));
          
      },
    handleCheckAllChange(val, index) {
      let _self = this;

      let mapArrcheckAllOptions = this.checkAllOptions[index];
      let newcheckAllOptions = mapArrcheckAllOptions.map((item, i) => item);

      let mapArrnoCheckAllOptions = this.noCheckAllOptions[index];
      let newnoCheckAllOptions = mapArrnoCheckAllOptions.map((item, i) => item);

      if (val) {
        //值为true，表示全选  使用 set 方式 从备用全选数组 拿值覆盖 该列所有行选择框v-model 绑定值
        this.$set(this.checkedOptions, index, newcheckAllOptions);
        let _rowData = _self.$refs.multipleTable.data; //对应的行
        for (let i = 0; i < _rowData.length; i++) {
          //遍历每一行
          for (let j = 0; j < this.checkedOptions.length; j++) {
            if (j != index) {
              //遍历所有列 如果不是选中列全部设置成FALSE
              _self.checkAll[j] = false; //表头设置成不勾选
              this.checkedOptions[j][i] = false; //其他未选中的列去清除选中状态
              // this.$set(this.checkedOptions, j, newnoCheckAllOptions);   //一行选中一个复选框后其他的都设置成未选中
            }
          }
        }
      } else {
        this.$set(this.checkedOptions, index, newnoCheckAllOptions);
      }
    },
    handlecheckedOptionsChange(val, row, rowIndex, index) {
      for (let j = 0; j < this.checkedOptions.length; j++) {
        if (j != index) {
          //遍历所有列 如果不是选中列全部设置成FALSE
          this.checkedOptions[j][rowIndex] = false; //一行选中一个复选框后其他的都设置成未选中
        }
      }
    //   this.checkAll[index]=true;
      // this.checkedOptions[index][row.$index]=true;
      this.$set(this.checkedOptions, index, this.checkedOptions[index]);

      
      for(let n=0;n<this.checkedOptions.length;n++){
          let isAllCheck=true;
          for(let x=0;x<this.checkedOptions[n].length;x++){
                let isCheck=this.checkedOptions[n][x];
                isAllCheck=isCheck&isAllCheck;
            
            }
            if(isAllCheck){
                this.checkAll[n]=true;
                this.checkAll=JSON.parse(JSON.stringify(this.checkAll));
                // this.$refs['headerCheck_'+index][0].checked=true;
            }else{
                this.checkAll[n]=false;
                this.checkAll=JSON.parse(JSON.stringify(this.checkAll));
                // this.$refs['headerCheck_'+index][0].checked=false;
            }
      }
        
      
      
    },
    saveButton() {
      let _self = this;

      var _retMap = new Map();
      let _checkedOptions = _self.checkedOptions; //对应的列
      let _rowData = _self.$refs.multipleTable.data; //对应的行

      // _rowData.forEach(item => {
      //   console.log(item.targetName);
      // });

      let retList = new Array();
      for (let i = 0; i < _rowData.length; i++) {
        let rowDataTemp = _rowData[i];
        var m = new Map();
        m.set("parentId", rowDataTemp.parentId);
        m.set("targetName", rowDataTemp.targetName);
        m.set("targetType", rowDataTemp.targetType);
        for (let j = 0; j < _checkedOptions.length; j++) {
          let obj = _checkedOptions[j];
          if (j == 0) {
            if (_checkedOptions[j][i]) {
              //console.log( this.$t('application.objectPermission1'));
              m.set("permission", 1);
            }
          } else if (j == 1) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 2);
            }
          } else if (j == 2) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 3);
            }
          } else if (j == 3) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 4);
            }
          } else if (j == 4) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 5);
            }
          } else if (j == 5) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 6);
            }
          } else if (j == 6) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 7);
            }
          } else if (j == 7) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 8);
            }
          } else if (j == 8) {
            if (_checkedOptions[j][i]) {
              m.set("permission", 9);
            }
          }
          console.log(_checkedOptions[j][i]);
        }
        retList.push(m);
      }
      console.log(retList);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: retList,
          url: "/acl/grantPermitBatch"
        })
        .then(function(response) {
          //console.log(JSON.stringify(response.data.data));
          if (response.data.code == 1) {
            _self.dialogVisible = false;
            _self.refreshData();
            _self.$message(_self.$t("message.updateSuccess"));
          } else {
            _self.$message.error(_self.$t("message.updateFailured"));
          }
        });
      //console.log(_self.$refs.multipleTable);
    },

    initCheckBoxData() {
      let res = {
        data: {
          headlist: [
            {
              tableHeaderName: this.$t("application.objectPermission1"),
              tableHeaderCode: "1"
            },
            {
              tableHeaderName: this.$t("application.objectPermission2"),
              tableHeaderCode: "2"
            },
            {
              tableHeaderName: this.$t("application.objectPermission3"),
              tableHeaderCode: "3"
            },
            {
              tableHeaderName: this.$t("application.objectPermission4"),
              tableHeaderCode: "4"
            },
            {
              tableHeaderName: this.$t("application.objectPermission5"),
              tableHeaderCode: "5"
            },
            {
              tableHeaderName: this.$t("application.objectPermission6"),
              tableHeaderCode: "6"
            },
            {
              tableHeaderName: this.$t("application.objectPermission7"),
              tableHeaderCode: "7"
            },
            {
              tableHeaderName: this.$t("application.objectPermission8"),
              tableHeaderCode: "8"
            },
            {
              tableHeaderName: this.$t("application.objectPermission9"),
              tableHeaderCode: "9"
            }
          ]
        }
      };

      let _self = this;
      _self.tableHeaderData = res.data.headlist;
      _self.permitList = [
        { targetType: "1", targetName: "everyone" },
        { targetType: "1", targetName: "owner" }
      ];
       _self.tableData = _self.permitList;
      for (let i = 0; i < _self.tableHeaderData.length; i++) {
        //定义 所需 数组对象
        var checkAllOptionsArray = new Array();
        var noCheckAllOptions = new Array();
        var checkedOptions = new Array();

        var dataColumn = _self.tableHeaderData[i];
        var checkLength = 0; // 排除
        for (var j = 0; j < _self.tableData.length; j++) {
          checkAllOptionsArray[j] = true; // 初始化全选备用二维数组 变量  表头全选 直接赋值 给列选择项控制数组
          noCheckAllOptions[j] = false; //初始化 取消全选变量

          let data = _self.tableData[j];
          if (dataColumn.tableHeaderCode == "1") {
            if (data["permission"] != 1) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "2") {
            if (data["permission"] != 2) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "3") {
            if (data["permission"] != 3) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "4") {
            if (data["permission"] != 4) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "5") {
            if (data["permission"] != 5) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "6") {
            if (data["permission"] != 6) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "7") {
            if (data["permission"] != 7) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "8") {
            if (data["permission"] != 8) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
          if (dataColumn.tableHeaderCode == "9") {
            if (data["permission"] != 9) {
              checkedOptions[j] = false;
            } else {
              checkedOptions[j] = true;
            }
          }
        }
        this.checkAllOptions[i] = checkAllOptionsArray; // 全选值 赋值  true
        this.noCheckAllOptions[i] = noCheckAllOptions; // 全不选  赋值 false
        this.checkedOptions[i] = checkedOptions; // 选择项  赋值
      }
    }
  }
};
</script>
<style scoped>
</style>