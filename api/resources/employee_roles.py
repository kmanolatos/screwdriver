from flask import Flask, jsonify, abort, make_response
from flask_restful import Api, Resource, reqparse, fields, marshal
from flasgger import swag_from
from flask_jwt_extended import jwt_required, get_jwt_identity

from app import db
from models import models

from resources.role_list import role_fields

class EmployeeRolesAPI(Resource):

    def __init__(self):
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument('Role_id', type=int, default="",
                                    location='json')
        super(EmployeeRolesAPI, self).__init__()

    @jwt_required
    @swag_from("apidocs/employee_roles_get.yml")
    def get(self, id):
        print("Get roles assigned to Emp = {}".format(id))

        emp_roles = db.session.query(models.Emp_Roles)\
            .filter(models.Emp_Roles.Emp_id == id)\
            .all()

        print(emp_roles)

        return {'Roles': [marshal(role.master_role, role_fields) for role in emp_roles]}

    @jwt_required
    @swag_from("apidocs/employee_roles_post.yml")
    def post(self, id):
        print("Assign new role to Emp = {}".format(id))
        args = self.reqparse.parse_args()
        print(args)
        emp_role = models.Emp_Roles(Emp_id=id, Role_id=args["Role_id"])
        db.session.add(emp_role)
        db.session.commit()

    # Consider Device deletion side-effects!
    @jwt_required
    @swag_from("apidocs/device_delete.yml")
    def delete(self, id):
        print("Delete id = {}".format(id))
        device = models.Devices.query.get(id)
        print(device)
        if device is None:
            abort(404)
        db.session.delete(device)
        db.session.commit()
        return {'result': True}