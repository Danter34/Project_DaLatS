using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SafeDalat_API.Migrations
{
    /// <inheritdoc />
    public partial class UDuser : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "ConsecutiveViolations",
                table: "Users",
                type: "int",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<DateTime>(
                name: "LockUntil",
                table: "Users",
                type: "datetime2",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "ConsecutiveViolations",
                table: "Users");

            migrationBuilder.DropColumn(
                name: "LockUntil",
                table: "Users");
        }
    }
}
